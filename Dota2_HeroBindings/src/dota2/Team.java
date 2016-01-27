package dota2;

public enum Team {
	Radiant, Dire, Unknown;
	
	public static Team fromId(int id) {
		switch(id) {
		case 2:
			return Radiant;
		case 3:
			return Dire;
		default:
			return Unknown;
		}
	}
}
