package dota2.game_info_sources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import skadistats.clarity.model.Entity;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.processor.runner.Runner;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.InputStreamSource;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.source.Source;
import util.StreamingFileInputStream;
import vconsole2.ConsolePacket;
import vconsole2.VConsole2;
import vconsole2.VConsoleListener;
import dota2.GameInfo;
import dota2.GameInfoSource;
import dota2.Hero;
import dota2.Player;
import dota2.PlayerInfo;
import dota2.Team;
import dota2.game_state_sources.VConsoleGameStateSource;

@UsesEntities
public class DemoGameInfoSource extends GameInfoSource implements VConsoleListener {
	private static Logger log = Logger.getLogger(DemoGameInfoSource.class.getName());
	
	private static final String DEMO_NAME = "HB_DEMO";
	private VConsole2 console;
	private String replay_dir;
	private Semaphore record_flag;
	private volatile boolean record_started;
	
	private Semaphore steamid_flag;
	private volatile long steamid;
	
	private Pattern pat_record_success;
	private Pattern pat_record_failure;
	private Pattern pat_player_steamid;
	
	public DemoGameInfoSource(VConsole2 console, String replay_dir) {
		super();
		this.console = console;
		this.replay_dir = replay_dir;
		
		this.record_flag = new Semaphore(1);
		this.record_started = false;
		
		this.steamid_flag = new Semaphore(1);
		this.steamid = -1;
		
		this.pat_record_success = Pattern.compile("(.*?)Recording to (.*?)", Pattern.DOTALL);
		this.pat_record_failure = Pattern.compile("(.*?)CDemoFile::Open: couldn't open file (.*?) for writing.(.*?)", Pattern.DOTALL);
		this.pat_player_steamid = Pattern.compile("(.*?)steamid(.*?):(.*?)\\[(.*?)\\](.*?)\\((?<id>\\d+)\\)(.*?)", Pattern.DOTALL);
		
		this.console.addListener(this);
	}
	
	@Override
	public void updateGameInfo() {
		// Delete the replay file if it exists
		File replay_file = new File(replay_dir, DEMO_NAME + ".dem");
		replay_file.delete();
		// Write the 'record' command
		try {
			this.record_flag.drainPermits();
			log.log(Level.FINER, "Sending record command");
			this.console.send(ConsolePacket.buildCommand("stop")).waitOn(); // stop in case another record command is still running for some unknown reasing, stop doesn't hurt anything if nothing is recording
			this.console.send(ConsolePacket.buildCommand("record " + DEMO_NAME)).waitOn();
			
			log.finer("Waiting for record command to process.");
			this.record_flag.acquire();
			if(record_started) {
				log.log(Level.FINER, "Opened demo file, stopping..");
				this.console.send(ConsolePacket.buildCommand("stop")).waitOn();
			} else {
				log.log(Level.FINER, "Error opening demo file, can't update game info.");
				return;
			}
		} catch (InterruptedException e) {
			log.log(Level.FINER, "Interrupted.", e);
		}
		
		// Get the player's steamid
		try {
			this.steamid_flag.drainPermits();
			this.console.send(ConsolePacket.buildCommand("status")).waitOn();
			this.steamid_flag.acquire();
		} catch (IllegalStateException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Replay parsing code, should probably put this somewhere else
		Source src = null;
		try {
			log.finer("Opening replay...");
			//src = new MappedFileSource(replay_file.getAbsolutePath());
			src = new InputStreamSource(new StreamingFileInputStream(replay_file));
			ControllableRunner r = new ControllableRunner(src).runWith(this);
			r.seek(0);
			r.halt();
			
			/* Build the GameInfo object */
			GameInfo gi = new GameInfo();
			
			log.finer("Current player has steamid: " + this.steamid);
			gi.setCurrentPlayer(new PlayerInfo(null, this.steamid, -1));
			
			// Get players
			List<Player> dota_players = new Vector<Player>();
			
			// Get player resource entity
			Entity pr = getEntity(r, "CDOTA_PlayerResource");
			if(pr != null) {
				// Get player objects
				List<Entity> players = getEntities(r, "CDOTAPlayer");
				for(Entity e : players) {
					int pindex = e.getProperty("m_iPlayerID");
					int team_id = e.getProperty("m_iTeamNum");
					int hero_id = pr.getProperty(String.format("m_vecPlayerTeamData.%04d.m_nSelectedHeroID", pindex)); 
					long steam_id = pr.getProperty(String.format("m_vecPlayerData.%04d.m_iPlayerSteamID", pindex));
					log.finest("\tFound player: " + steam_id);
					dota_players.add(new Player(steam_id, (hero_id != -1 ? Hero.fromID(hero_id) : null), Team.fromId(team_id)));
					//playerObjects.put(pindex, new DotaPlayer(steam_id, pindex, hero_id));
				}
			}
				
			gi.setPlayers(dota_players);
			log.finer("...sending gi");
			super.ref.updateGameInfo(gi);
			src.close();
		} catch (IOException e) {
			log.log(Level.WARNING, "Error reading replay.", e);
		} finally {
			//replay_file.delete();
		}
		
	}

    private Entity getEntity(Runner runner, String entityName) {
        return runner.getContext().getProcessor(Entities.class).getByDtName(entityName);
    }
    
    private List<Entity> getEntities(Runner runner, String entityName) {
    	List<Entity> entities = new Vector<Entity>();
    	Iterator<Entity> _it = runner.getContext().getProcessor(Entities.class).getAllByDtName(entityName);
    	while(_it.hasNext()) {
    		
    		entities.add(_it.next());
    	}
    	return entities;
    }

	@Override
	public void onPacketReceived(ConsolePacket packet) {
		String s = new String(packet.getPayload(), StandardCharsets.UTF_8);
		Matcher m_success = pat_record_success.matcher(s);
		Matcher m_failure = pat_record_failure.matcher(s);
		if(m_success.matches()) {
			this.record_started = true;
			this.record_flag.release();
		}
		if(m_failure.matches()) {
			this.record_started = false;
			this.record_flag.release();
		}
		Matcher m_steamid = pat_player_steamid.matcher(s);
		if(m_steamid.matches()) {
			this.steamid = Long.parseLong(m_steamid.group("id"));
			this.steamid_flag.release();
		}
		//System.out.println(m_success.matches() + " _ " + m_failure.matches() + " : " + s);
	}
	
}
