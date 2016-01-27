package dota2.player_sources;

import vconsole2.VConsole2;
import dota2.Game;
import dota2.GameInfo;
import dota2.GameListener;
import dota2.GameState;
import dota2.PlayerSource;

public class VConsoleAssistedDemoPlayerSource extends PlayerSource implements GameListener, Runnable {
	
	private static final String demo_name = "HB_DEMO";
	
	private Game game;
	private VConsole2 console;
	private String demo_dir;
	
	private Thread worker;
	
	public VConsoleAssistedDemoPlayerSource(Game game, VConsole2 console, String demo_dir) {
		super();
		this.game = game;
		this.console = console;
		this.demo_dir = demo_dir;
		
		this.worker = new Thread(this);
		this.worker.start();
	}
	
	@Override
	public void onStateChange(GameState prev, GameState cur) {
		if(cur == GameState.PRE_GAME) {
			
		}
	}
	
	@Override
	public void run() {
		while(Thread.interrupted() == false) {
			
		}
	}

	@Override
	public void onInfoChange(GameInfo prev, GameInfo cur) {
		
	}
	
	
}
