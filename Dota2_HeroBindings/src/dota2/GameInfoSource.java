package dota2;

public abstract class GameInfoSource {

	protected Game ref;
	
	public Game getGame() { return ref; }
	
	public void setGame(Game ref) { this.ref = ref; }
	
	public GameInfoSource() {
		this.ref = null;
	}
	
	public abstract void updateGameInfo();
	
}
