package dota2;

public class Player {
	
	private Hero current_hero; // null = no assigned hero
	private Team current_team;
	private long steam_id;
	private String nickname;
	
	public long getSteamID() { return steam_id; }
	public void setSteamID(long steam_id) { this.steam_id = steam_id; }
	
	public Hero getCurrentHero() { return current_hero; }
	public void setCurrentHero(Hero current_hero) { this.current_hero = current_hero; }
	
	public Team getCurrentTeam() { return current_team; }
	public void setCurrentTeam(Team current_team) { this.current_team = current_team; }
	
	public Player(long steam_id, Hero current_hero, Team current_team) {
		this.steam_id = steam_id;
		this.current_hero = current_hero;
		this.current_team = current_team;
	}
	
	public static Player fromSteamID(long steam_id) {
		return new Player(steam_id, null, null);
	}
	
	@Override
	public String toString() {
		return String.format("{%d,%s,%s}", steam_id, current_team.toString(), current_hero.toString());
	}
}
