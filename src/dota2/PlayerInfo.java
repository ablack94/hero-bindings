package dota2;

public class PlayerInfo {
	
	protected String nickname; // null if unknown
	protected long steam_id; // -1 if unknown
	protected int position; // position on the scoreboard, [0, 19] from left to right ,-1 if unknown 
	
	public String getNickname() { return this.nickname; }
	public long getSteamID() { return this.steam_id; }
	public int getPosition() { return this.position; }
	
	public PlayerInfo(String nickname, long steam_id, int position) {
		this.nickname = nickname;
		this.steam_id = steam_id;
		this.position = position;
	}
	
	
}
