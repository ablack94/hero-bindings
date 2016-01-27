/* Andrew Black
 * January 25, 2015
 * Represents the known state of the game.
 */

package dota2;

import java.util.concurrent.LinkedBlockingQueue;

public class Game {
	
	private GameState state; // can be null for unknown
	private GameInfo info; // can be null for unknown
	
	private GameStateSource gs_src;
	private GameInfoSource gi_src;
	
	private LinkedBlockingQueue<GameListener> game_listeners;
	
	/* Accessors/Mutators */
	public GameState getState() { return state; }
	
	public Game(GameStateSource gs_src, GameInfoSource gi_src) {
		this.gs_src = gs_src;
		this.gs_src.setGame(this);
		
		this.gi_src = gi_src;
		this.gi_src.setGame(this);
		
		this.state = null;
		this.game_listeners = new LinkedBlockingQueue<GameListener>();
	}
	
	/* Functions */
	public void addGameListener(GameListener listener) {
		this.game_listeners.add(listener);
	}
	
	private void notifyGameListeners_onStateChange(GameState prev, GameState cur) {
		for(GameListener listener : game_listeners) {
			try {
				listener.onStateChange(prev, cur);
			} catch(Exception e) {
				System.err.println("Error notifying game listeners onStateChange");
				e.printStackTrace();
			}
		}
	}
	
	private void notifyGameListeners_onInfoChange(GameInfo prev, GameInfo cur) {
		for(GameListener listener : game_listeners) {
			try {
				listener.onInfoChange(prev, cur);
			} catch(Exception e) {
				System.err.println("Error notifying game listeners onInfoChange");
				e.printStackTrace();
			}
		}
	}
	
	public void updateState(GameState new_state) {
		GameState prev = this.state;
		this.state = new_state;
		notifyGameListeners_onStateChange(prev, new_state);
	}
	
	public void updateGameInfo(GameInfo new_info) {
		GameInfo prev = this.info;
		this.info = new_info;
		notifyGameListeners_onInfoChange(prev, new_info);
	}
	
}
