/* Andrew Black
 * January 26, 2015
 * Represents all of the information obtained by recording a short replay, and analyzing it using the Clarity library.
 * This is basically just a giant batch call that retrieves and parses all of the game state that I can from the replay.
 * I'm assuming that it's fairly expensive to record a replay, read it from the disk, and process it, 
 * 	so this minimizes the effect on the user's system.
 */

package dota2;

import java.util.List;

public class GameInfo {
	
	private PlayerInfo this_player;
	
	public PlayerInfo getCurrentPlayer() { return this_player; }
	public void setCurrentPlayer(PlayerInfo player) { this_player = player; }
	
	private List<Player> players;
	
	public List<Player> getPlayers() { return players; }
	public void setPlayers(List<Player> players) { this.players = players; }
	
	public GameInfo() {
		this.this_player = null;
		this.players = null;
	}
	
	public GameInfo(List<Player> players) {
		this.players = players;
	}
	
}
