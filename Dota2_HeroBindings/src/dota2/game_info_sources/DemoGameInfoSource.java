package dota2.game_info_sources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import skadistats.clarity.model.Entity;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.processor.runner.Runner;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.InputStreamSource;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.source.Source;
import vconsole2.ConsolePacket;
import vconsole2.VConsole2;
import dota2.GameInfo;
import dota2.GameInfoSource;
import dota2.Hero;
import dota2.Player;
import dota2.Team;

@UsesEntities
public class DemoGameInfoSource extends GameInfoSource {
	private static final String DEMO_NAME = "HB_DEMO";
	private VConsole2 console;
	private String replay_dir;
	
	public DemoGameInfoSource(VConsole2 console, String replay_dir) {
		super();
		this.console = console;
		this.replay_dir = replay_dir;
	}
	
	@Override
	public void updateGameInfo() {
		// Write the 'record' command
		try {
			this.console.send(ConsolePacket.buildCommand("record " + DEMO_NAME));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Wait a few seconds
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Write the 'stop' command
		try {
			this.console.send(ConsolePacket.buildCommand("stop"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Read the replay file
		File replay_file = new File(replay_dir, DEMO_NAME + ".dem");
		// Replay parsing code, should probably put this somewhere else
		try {
			Source src = new MappedFileSource(replay_file.getAbsolutePath());
			SimpleRunner r = new SimpleRunner(src);
			r.runWith(this);
			
			// Get player resource entity
			Entity pr = getEntity(r, "CDOTA_PlayerResource");
			assert(pr != null);
			// Get player objects
			List<Entity> players = getEntities(r, "CDOTAPlayer");
			assert(players.size() > 0);
			
			/* Build the GameInfo object */
			GameInfo gi = new GameInfo();
			// Get players
			List<Player> dota_players = new Vector<Player>();
			for(Entity e : players) {
				int pindex = e.getProperty("m_iPlayerID");
				int team_id = e.getProperty("m_iTeamNum");
				int hero_id = pr.getProperty(String.format("m_vecPlayerTeamData.%04d.m_nSelectedHeroID", pindex)); 
				long steam_id = pr.getProperty(String.format("m_vecPlayerData.%04d.m_iPlayerSteamID", pindex));
				dota_players.add(new Player(steam_id, (hero_id != -1 ? Hero.fromID(hero_id) : null), Team.fromId(team_id)));
				//playerObjects.put(pindex, new DotaPlayer(steam_id, pindex, hero_id));
			}
			
			gi.setPlayers(dota_players);
			super.ref.updateGameInfo(gi);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			replay_file.delete();
		}
		
	}

    private Entity getEntity(Runner runner, String entityName) {
    	assert(runner != null);
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
	
}
