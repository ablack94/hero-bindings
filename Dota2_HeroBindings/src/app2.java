import java.io.IOException;
import java.net.UnknownHostException;

import vconsole2.VConsole2;
import dota2.Game;
import dota2.GameInfo;
import dota2.GameInfoSource;
import dota2.GameListener;
import dota2.GameState;
import dota2.Player;
import dota2.game_info_sources.DemoGameInfoSource;
import dota2.game_state_sources.VConsoleGameStateSource;


public class app2 implements GameListener {

	private GameInfoSource gi_src;
	
	public app2() throws UnknownHostException, IOException {
		VConsole2 console = new VConsole2();
		
		VConsoleGameStateSource gs_src = new VConsoleGameStateSource(console);
		gi_src = new DemoGameInfoSource(console, "D:\\SteamLibrary\\steamapps\\common\\dota 2 beta\\game\\dota");
		Game game = new Game(gs_src, gi_src);
		game.addGameListener(this);
		
		console.start();
		
		while(true);
	}
	
	@Override
	public void onStateChange(GameState prev, GameState cur) {
		String _prev = (prev != null) ? prev.toString() : "None";
		String _cur = (cur != null) ? cur.toString() : "None";
		System.out.println("State change: " + _prev + " -> " + _cur);
		if(cur == GameState.PRE_GAME || cur == GameState.GAME_IN_PROGRESS) {
			System.out.println("Updating game info...");
			gi_src.updateGameInfo();
		}
	}
	
	@Override
	public void onInfoChange(GameInfo prev, GameInfo cur) {
		System.out.println("New info!");
		for(Player p : cur.getPlayers()) {
			System.out.println("\t" + p.toString());
			//System.out.println("\t" + p.getCurrentHero());
		}
		
	}
	
	public static void main(String[] argv) throws Exception {
		app2 app = new app2();
	}


}
