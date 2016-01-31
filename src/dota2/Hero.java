/* Andrew Black
 * Generated code, do not modify.
 */

package dota2;

import java.util.NoSuchElementException;

public enum Hero {
	Antimage("antimage",1),
	Axe("axe",2),
	Bane("bane",3),
	Bloodseeker("bloodseeker",4),
	CrystalMaiden("crystal_maiden",5),
	DrowRanger("drow_ranger",6),
	Earthshaker("earthshaker",7),
	Juggernaut("juggernaut",8),
	Mirana("mirana",9),
	Morphling("morphling",10),
	Nevermore("nevermore",11),
	PhantomLancer("phantom_lancer",12),
	Puck("puck",13),
	Pudge("pudge",14),
	Razor("razor",15),
	SandKing("sand_king",16),
	StormSpirit("storm_spirit",17),
	Sven("sven",18),
	Tiny("tiny",19),
	Vengefulspirit("vengefulspirit",20),
	Windrunner("windrunner",21),
	Zuus("zuus",22),
	Kunkka("kunkka",23),
	Lina("lina",25),
	Lion("lion",26),
	ShadowShaman("shadow_shaman",27),
	Slardar("slardar",28),
	Tidehunter("tidehunter",29),
	WitchDoctor("witch_doctor",30),
	Lich("lich",31),
	Riki("riki",32),
	Enigma("enigma",33),
	Tinker("tinker",34),
	Sniper("sniper",35),
	Necrolyte("necrolyte",36),
	Warlock("warlock",37),
	Beastmaster("beastmaster",38),
	Queenofpain("queenofpain",39),
	Venomancer("venomancer",40),
	FacelessVoid("faceless_void",41),
	SkeletonKing("skeleton_king",42),
	DeathProphet("death_prophet",43),
	PhantomAssassin("phantom_assassin",44),
	Pugna("pugna",45),
	TemplarAssassin("templar_assassin",46),
	Viper("viper",47),
	Luna("luna",48),
	DragonKnight("dragon_knight",49),
	Dazzle("dazzle",50),
	Rattletrap("rattletrap",51),
	Leshrac("leshrac",52),
	Furion("furion",53),
	LifeStealer("life_stealer",54),
	DarkSeer("dark_seer",55),
	Clinkz("clinkz",56),
	Omniknight("omniknight",57),
	Enchantress("enchantress",58),
	Huskar("huskar",59),
	NightStalker("night_stalker",60),
	Broodmother("broodmother",61),
	BountyHunter("bounty_hunter",62),
	Weaver("weaver",63),
	Jakiro("jakiro",64),
	Batrider("batrider",65),
	Chen("chen",66),
	Spectre("spectre",67),
	AncientApparition("ancient_apparition",68),
	DoomBringer("doom_bringer",69),
	Ursa("ursa",70),
	SpiritBreaker("spirit_breaker",71),
	Gyrocopter("gyrocopter",72),
	Alchemist("alchemist",73),
	Invoker("invoker",74),
	Silencer("silencer",75),
	ObsidianDestroyer("obsidian_destroyer",76),
	Lycan("lycan",77),
	Brewmaster("brewmaster",78),
	ShadowDemon("shadow_demon",79),
	LoneDruid("lone_druid",80),
	ChaosKnight("chaos_knight",81),
	Meepo("meepo",82),
	Treant("treant",83),
	OgreMagi("ogre_magi",84),
	Undying("undying",85),
	Rubick("rubick",86),
	Disruptor("disruptor",87),
	NyxAssassin("nyx_assassin",88),
	NagaSiren("naga_siren",89),
	KeeperOfTheLight("keeper_of_the_light",90),
	Wisp("wisp",91),
	Visage("visage",92),
	Slark("slark",93),
	Medusa("medusa",94),
	TrollWarlord("troll_warlord",95),
	Centaur("centaur",96),
	Magnataur("magnataur",97),
	Shredder("shredder",98),
	Bristleback("bristleback",99),
	Tusk("tusk",100),
	SkywrathMage("skywrath_mage",101),
	Abaddon("abaddon",102),
	ElderTitan("elder_titan",103),
	LegionCommander("legion_commander",104),
	Techies("techies",105),
	EmberSpirit("ember_spirit",106),
	EarthSpirit("earth_spirit",107),
	Terrorblade("terrorblade",109),
	Phoenix("phoenix",110),
	Oracle("oracle",111),
	WinterWyvern("winter_wyvern",112),
	ArcWarden("arc_warden",113);
	
	private final String name;
	private final int id;
	Hero(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	public String getName() { return name; }
	public int getID() { return id; }
	
	public static Hero fromName(String name) {
		Hero rval = null;
		for(Hero h : Hero.values()) {
			if(h.getName().equalsIgnoreCase(name)) {
				rval = h;
				break;
			}
		}
		if(rval == null) {
			throw new NoSuchElementException("No hero with name '" + name + "' exists.");
		}
		return rval;
	}
	
	public static Hero fromID(int id) {
		Hero rval = null;
		for(Hero h : Hero.values()) {
			if(h.getID() == id) {
				rval = h;
				break;
			}
		}
		if(rval == null) {
			throw new NoSuchElementException("No hero with ID '" + id + "' exists.");
		}
		return rval;
	}
}
