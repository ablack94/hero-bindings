/* Andrew Black
 * January 25, 2015
 * Abstract class used to update a Game object's GameState
 */

package dota2;

public abstract class GameStateSource {
	protected Game ref;
	
	public Game getGame() { return ref; }
	
	public void setGame(Game ref) { this.ref = ref; }
	
	public GameStateSource() {
		this.ref = null;
	}
	
	public abstract void unbind();
}
