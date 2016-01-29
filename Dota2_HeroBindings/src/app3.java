import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

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


public class app3 implements GameListener {

	private GameInfoSource gi_src;
	VConsole2 console;
	long this_steam_id;
	Player cur_this_player;
	Player prev_this_player;
	Map<Hero, String> hero_config_map;
	String default_config = "HB_default.cfg";
	
	public app3(long steam_id) throws Exception {
		System.out.println("Monitoring player with steamID: " + steam_id);
		// Create default config files if none exist
		hero_config_map = new HashMap<Hero, String>();
		hero_config_map.put(Hero.Queenofpain, "HB_qop.cfg");
		
		this_steam_id = steam_id;
		prev_this_player = Player.fromSteamID(steam_id);
		cur_this_player = Player.fromSteamID(steam_id);
		
		console = new VConsole2();
		while(true) {
			try {
				console.connect();
			} catch(IOException e) {
				System.out.println("Connection failed...");
				Thread.sleep(5000);
				continue;
			}
			break;
		}
		
		VConsoleGameStateSource gs_src = new VConsoleGameStateSource(console);
		gi_src = new DemoGameInfoSource(console, ".");
		Game game = new Game(gs_src, gi_src);
		game.addGameListener(this);
		
		while(true);
	}
	
	@Override
	public void onStateChange(GameState prev, GameState cur) {
		String _prev = (prev != null) ? prev.toString() : "None";
		String _cur = (cur != null) ? cur.toString() : "None";
		System.out.println("State change: " + _prev + " -> " + _cur);
		
		if(cur == GameState.PRE_GAME || cur == GameState.GAME_IN_PROGRESS) {
			System.out.println("Updating game info...");
			new Thread(new Runnable() {
				public void run() {
					gi_src.updateGameInfo();
				}
			}).start();
		}
	}
	
	@Override
	public void onInfoChange(GameInfo prev, GameInfo cur) {
		System.out.println("NEW INFO MAIN " + cur);
		for(Player p : cur.getPlayers()) {
			System.out.println("\t\t" + p.getSteamID() + " == " + this_steam_id);
			if(p.getSteamID() == this_steam_id) {
				prev_this_player = cur_this_player;
				cur_this_player = p;
				if(cur_this_player.getCurrentHero() != prev_this_player.getCurrentHero()) {
					System.out.println("Assigned hero: " + cur_this_player.getCurrentHero());
					String c = hero_config_map.getOrDefault(cur_this_player.getCurrentHero(), default_config);
					System.out.println("\tLoading: " + c);
					loadConfig(c);
				}
			}
		}
		
	}
	
	private void loadConfig(String config) {
		try {
			console.send(ConsolePacket.buildCommand("exec " + config)).waitOn();
		} catch (IllegalStateException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] argv) throws Exception {
		app3 app = new app3(Long.parseLong(argv[0]));
	}


}
