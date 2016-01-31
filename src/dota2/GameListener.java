package dota2;

public interface GameListener {
	public void onStateChange(GameState prev, GameState cur);
	public void onInfoChange(GameInfo prev, GameInfo cur);
	
}
