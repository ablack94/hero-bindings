import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import skadistats.clarity.model.Entity;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.source.InputStreamSource;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.source.Source;


@UsesEntities
public class app {
	
	private enum Team {
		Radiant, Dire, Unknown;
		
		public Team fromId(int id) {
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
	
	private class DotaPlayer {
		public long steam_id;
		public int pindex;
		public int hero_id;
		public DotaPlayer(long steam_id, int pindex, int hero_id) {
			this.steam_id = steam_id;
			this.pindex = pindex;
			this.hero_id = hero_id;
		}
	}
	
	ControllableRunner runner;
	
	public app(String[] argv) throws Exception {
		System.out.println(argv[0]);
		Source src = new MappedFileSource(argv[0]);
		runner = new ControllableRunner(src).runWith(this);
		runner.seek(runner.getLastTick());
		runner.halt();
		
		// Get player resource entity
		Entity pr = getEntity("CDOTA_PlayerResource");
		System.out.println(pr);
		//assert(pr != null);
		// Get player objects
		List<Entity> players = getEntities("CDOTAPlayer");
		//assert(players.size() > 0);
		
		Map<Integer,DotaPlayer> playerObjects = new HashMap<Integer,DotaPlayer>();
		for(Entity e : players) {
			int pindex = e.getProperty("m_iPlayerID");
			int hero_id = pr.getProperty(String.format("m_vecPlayerTeamData.%04d.m_nSelectedHeroID", pindex)); 
			long steam_id = pr.getProperty(String.format("m_vecPlayerData.%04d.m_iPlayerSteamID", pindex));
			playerObjects.put(pindex, new DotaPlayer(steam_id, pindex, hero_id));
			if(pindex == 0)
			System.out.printf("%d\n\t%d,%d\n", steam_id, pindex, hero_id);
		}
	}
	
    private Entity getEntity(String entityName) {
        return runner.getContext().getProcessor(Entities.class).getByDtName(entityName);
    }
    
    private List<Entity> getEntities(String entityName) {
    	List<Entity> entities = new Vector<Entity>();
    	Iterator<Entity> _it = runner.getContext().getProcessor(Entities.class).getAllByDtName(entityName);
    	while(_it.hasNext()) {
    		
    		entities.add(_it.next());
    	}
    	return entities;
    }
	
	// Entry point
	public static void main(String[] argv) throws Exception {
		app a = new app(argv);
		/*
		WatchService watcher = FileSystems.getDefault().newWatchService();
		File ifile = new File(argv[0]);
		Path dir = Paths.get(ifile.getParent());
		dir.register(watcher, java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY);
		
		while(true) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch(InterruptedException e) {
				return;
			}
			
			for(WatchEvent<?> event : key.pollEvents()) {
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path filename = ev.context();
				System.out.printf("%s modified\n", filename);
			}
			
			//app a = new app(argv);
			//Thread.sleep(100);
		}*/
	}
	
}
