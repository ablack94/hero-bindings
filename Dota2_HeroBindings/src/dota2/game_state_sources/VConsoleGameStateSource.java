package dota2.game_state_sources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vconsole2.ConsolePacket;
import vconsole2.VConsole2;
import vconsole2.VConsoleListener;
import dota2.Game;
import dota2.GameState;
import dota2.GameStateSource;

public class VConsoleGameStateSource extends GameStateSource implements VConsoleListener {
	private static Logger log = Logger.getLogger(VConsoleGameStateSource.class.getName());
	
	private VConsole2 console;
	private List<Pattern> patterns;
	private Matcher m;
	
	public VConsoleGameStateSource(VConsole2 console) {
		super();
		this.console = console;
		this.patterns = new Vector<Pattern>();
		this.patterns.add(Pattern.compile("(.*?)C:Gamerules: entering state '(?<state>\\w+)'(.*?)", Pattern.DOTALL));
		this.patterns.add(Pattern.compile("(.*?)GameState: (?<state>\\w+)(.*?)", Pattern.DOTALL));
		this.m = null;
		
		this.console.addListener(this);
		
		// When we first start, we sent the 'status' command to the console, which will print the current game state if we're in a game
		try {
			this.console.send(ConsolePacket.buildCommand("status")).waitOn();
		} catch (IllegalStateException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onPacketReceived(ConsolePacket packet) {
		String data = new String(packet.getPayload(), StandardCharsets.UTF_8);
		m = null;
		for(Pattern p : patterns) {
			m = p.matcher(data);
			if(m.matches()) {
				break;
			}
			m = null;
		}
		if(m != null) {
			GameState gs = null;
			try {
				gs = GameState.fromString(m.group("state"));
			} catch(IllegalArgumentException e) {
				log.log(Level.WARNING, "Game state '" + m.group(2) + "' was matched, but is not recognized. This is probably a bug, report it or check the regex being used!\nRaw data: " + data, e);
			}
			if(gs != null) {
				if(super.ref.getState() != gs) {
					log.log(Level.FINE, "Detected gamestate transition '" + gs.toString() + "'");
					super.ref.updateState(gs);
				}
			}
		}
	}
	
}
