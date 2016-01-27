package dota2;

public class PlayerSource {
	protected Player ref;
	
	public Player getPlayer() { return ref; }
	
	public void setPlayer(Player ref) { this.ref = ref; }
	
	public PlayerSource() {
		this.ref = null;
	}
}
