package dota2;

/* Andrew Black
 * January 25, 2015
 * Dota game state enum.
 * Source: DOTA_GameState at https://developer.valvesoftware.com/wiki/Dota_2_Workshop_Tools/Scripting/API#CDOTA_BaseNPC_Hero
 */

public enum GameState {
	INIT,
	WAIT_FOR_PLAYERS_TO_LOAD,
	CUSTOM_GAME_SETUP,
	HERO_SELECTION,
	STRATEGY_TIME,
	TEAM_SHOWCASE,
	PRE_GAME,
	GAME_IN_PROGRESS,
	POST_GAME,
	DISCONNECT;
	
	public static GameState fromString(String s) throws IllegalArgumentException {
		switch(s.toUpperCase()) {
		case "DOTA_GAMERULES_STATE_INIT":
			return GameState.INIT;
		case "DOTA_GAMERULES_STATE_WAIT_FOR_PLAYERS_TO_LOAD":
			return GameState.WAIT_FOR_PLAYERS_TO_LOAD;
		case "DOTA_GAMERULES_STATE_CUSTOM_GAME_SETUP":
			return GameState.CUSTOM_GAME_SETUP;
		case "DOTA_GAMERULES_STATE_HERO_SELECTION":
			return GameState.HERO_SELECTION;
		case "DOTA_GAMERULES_STATE_STRATEGY_TIME":
			return GameState.STRATEGY_TIME;
		case "DOTA_GAMERULES_STATE_TEAM_SHOWCASE":
			return GameState.TEAM_SHOWCASE;
		case "DOTA_GAMERULES_STATE_PRE_GAME":
			return GameState.PRE_GAME;
		case "DOTA_GAMERULES_STATE_GAME_IN_PROGRESS":
			return GameState.GAME_IN_PROGRESS;
		case "DOTA_GAMERULES_STATE_POST_GAME":
			return GameState.POST_GAME;
		case "DOTA_GAMERULES_STATE_DISCONNECT":
			return GameState.DISCONNECT;
		default:
			throw new IllegalArgumentException("State '" + s + "' does not exist.");
		}
	}
}
