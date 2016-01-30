import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import vconsole2.ConsolePacket;
import vconsole2.VConsole2;
import dota2.Game;
import dota2.GameInfo;
import dota2.GameInfoSource;
import dota2.GameListener;
import dota2.GameState;
import dota2.Hero;
import dota2.Player;
import dota2.game_info_sources.DemoGameInfoSource;
import dota2.game_state_sources.VConsoleGameStateSource;


public class HBV1 implements GameListener {
	private static Logger log = Logger.getLogger(HBV1.class.getName());
	
	private GameInfoSource gi_src;
	VConsole2 console;
	long this_steam_id;
	String dota2_path;
	Player cur_this_player;
	Player prev_this_player;
	Map<Hero, String> hero_config_map;
	String default_config = "HB_default.cfg";
	
	String dota2_cfg_path;
	
	public HBV1(String root_dir, String config_file) throws InterruptedException, IllegalStateException, IOException {
		// Open the config file and read the data to a JSONObject
		JSONObject config_root = null;
		try {
			FileInputStream config_fis = new FileInputStream(config_file);
			config_root = (JSONObject)JSONValue.parseWithException(config_fis);
		} catch(FileNotFoundException fne) {
			log.severe("Configuration file '" + config_file + "' does not exist. Terminating.");
			return;
		} catch(IOException ioe) {
			log.log(Level.SEVERE, "Error loading configuration file. Terminating.", ioe);
			return;
		} catch(ParseException pe) {
			log.log(Level.SEVERE, "Error in the configuration file format. Terminating.", pe);
		}
		// Read the fields of the config file
		long _steam_id;
		try {
			_steam_id = new Long((String)config_root.getOrDefault("steamid", -1));
		} catch(Exception e) {
			log.severe("steamid must be a string in the configuration file!");
			return;
		}
		String _dota_path;
		try {
			_dota_path = (String)config_root.getOrDefault("dota2", null);
		} catch(Exception e) {
			log.severe("dota path must be a string!");
			return;
		}
		// Check
		if(_steam_id == -1) {
			log.severe("Invalid/Missing SteamID. Terminating.");
			return;
		}
		if(_dota_path == null) {
			log.severe("Missing dota 2 path. Terminating.");
			return;
		}
		if(new File(_dota_path).exists() == false) {
			log.severe("Invalid dota 2 path, the directory does not exist. '" + _dota_path + "'");
			return;
		}
		// Configuration has been validated at this point
		this.this_steam_id = _steam_id;
		this.dota2_path = _dota_path;
		
		log.info("===================================================");
		log.info("Monitoring player with steamID: " + this.this_steam_id);
		log.info("Dota2 path: '" + this.dota2_path + "'");
		log.info("===================================================");
		
		this.dota2_cfg_path = new File(this.dota2_path, "game\\dota\\cfg").getAbsolutePath();
		
		prev_this_player = Player.fromSteamID(this_steam_id);
		cur_this_player = Player.fromSteamID(this_steam_id);
		
		console = new VConsole2();
		log.info("Trying to connect to dota 2...");
		while(console.connect() == false) {
			Thread.sleep(5000);
		}
		// Send the clear command, and reconnect
		log.info("Connected! Clearing buffers and reconnecting...");
		console.send(ConsolePacket.buildCommand("clear")).waitOn();
		console.disconnect();
		log.info("Reconnecting...");
		while(console.connect() == false) {
			Thread.sleep(5000);
		}
		log.info("Connected! Running application...");
		
		VConsoleGameStateSource gs_src = new VConsoleGameStateSource(console);
		gi_src = new DemoGameInfoSource(console, new File(this.dota2_path, "game\\dota").getAbsolutePath());
		Game game = new Game(gs_src, gi_src);
		game.addGameListener(this);
		
		while(true);
	}
	
	@Override
	public void onStateChange(GameState prev, GameState cur) {
		String _prev = (prev != null) ? prev.toString() : "None";
		String _cur = (cur != null) ? cur.toString() : "None";
		log.fine("Game state change detected: " + _prev + " -> " + _cur);
		
		if(cur == GameState.PRE_GAME || cur == GameState.GAME_IN_PROGRESS) {
			log.fine("Updating game info...");
			new Thread(new Runnable() {
				public void run() {
					gi_src.updateGameInfo();
				}
			}).start();
		}
	}
	
	@Override
	public void onInfoChange(GameInfo prev, GameInfo cur) {
		boolean player_found = false;
		for(Player p : cur.getPlayers()) {
			if(p.getSteamID() == this_steam_id) {
				player_found = true;
				prev_this_player = cur_this_player;
				cur_this_player = p;
				if(cur_this_player.getCurrentHero() != prev_this_player.getCurrentHero()) {
					log.info("Assigned hero: " + cur_this_player.getCurrentHero());
					//String c = hero_config_map.getOrDefault(cur_this_player.getCurrentHero(), default_config);
					String c = "hb_" + cur_this_player.getCurrentHero().getName();
					if(new File(this.dota2_cfg_path, c).exists() == false) {
						log.info("No hero-specific bindings for " + cur_this_player.getCurrentHero().getName() + " loading default...");
						loadConfig("hb_default.cfg");
						// Alert the user of hb_default.cfg can't be found
						if(new File(this.dota2_cfg_path, "hb_default.cfg").exists() == false) {
							log.warning("Default bindings file doesn't exist! Gameplay may be affected. Make sure hb_default.cfg exists in your cfg directory!");
						}
					} else {
						log.info("Loading configuration '" + c + "' for hero " + cur_this_player.getCurrentHero().getName());
						loadConfig(c);
					}
				}
			}
		}
		if(player_found == false) {
			log.severe("We found player info for your game, but couldn't find you! Your steamid is most likely incorrect.");
		}
	}
	
	private void loadConfig(String config) {
		try {
			console.send(ConsolePacket.buildCommand("exec " + config)).waitOn();
		} catch (IllegalStateException | InterruptedException e) {
			log.log(Level.SEVERE, "Error sending load configuration command... might need to restart the application.", e);
		}
	}
	
	public static void main(String[] argv) throws Exception {
		// Setup logging
		LogManager.getLogManager().readConfiguration(HBV1.class.getResourceAsStream("resources/logcfg.properties"));
		
		boolean run = true;
		// Make a home for us if we don't already have one
		File dir = new File(System.getProperty("user.home"), "Dota2HeroBindings");
		String root_dir = dir.getAbsolutePath();
		if(dir.exists() == false) {
			dir.mkdir();
		}
		
		// Determine config file path to use
		String config_file = "config.json";
		if(argv.length > 0) {
			if(new File(argv[0]).exists()) {
				config_file = argv[0];
			} else {
				log.severe("Error, config file '" + argv[0] + "' does not exist. Terminating.");
				run = false;
			}
		} else {
			File cfg = new File(root_dir, config_file);
			if(cfg.exists() == false) {
				log.warning("Default configuration not detected, creating one...");
				try {
					InputStream is = HBV1.class.getResourceAsStream("resources/default_config.json");
					FileOutputStream fos = new FileOutputStream(cfg);
					for(int i=is.read();i!=-1;i=is.read()) { fos.write(i); }
				} catch(Exception e) {
					log.log(Level.SEVERE, "Unable to create default configuration file '" + cfg.getAbsolutePath() + "'", e);
					run = false;
				}
				run = false;
			}
		}
		// Run
		if(run) {
			try {
				HBV1 app = new HBV1(root_dir, new File(dir, config_file).getAbsolutePath());
			} catch(Exception e) {
				log.log(Level.SEVERE, "Unhandled exception. Terminating.", e);
			}
		}
		System.exit(0); // This is important, without this we could hang if other threads are running
	}


}
