import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import skadistats.clarity.model.CombatLogEntry;
import skadistats.clarity.model.Entity;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.gameevents.OnCombatLogEntry;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.ControllableRunner;
import skadistats.clarity.processor.runner.Runner;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.InputStreamSource;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.source.Source;
import util.StreamingFileInputStream;

@UsesEntities
public class stream_test {
	
	public stream_test(String name) throws Exception {
		
		StreamingFileInputStream sin = new StreamingFileInputStream(new File(name));
		Source src = new InputStreamSource(sin);
		ControllableRunner runner = new ControllableRunner(src).runWith(this);
		
		while(true) {
			while(runner.isAtEnd() == true) {
				System.out.println("Waiting...");
				Thread.sleep(100);
			}
			while(runner.isAtEnd() == false) {
				//System.out.println("\tTick " + runner.getTick());
				runner.tick();
			}
		}
		
		/*
		FileInputStream fis = new FileInputStream(new File(name));
		while(true) {
			System.out.println(fis.getChannel().size());
			Thread.sleep(500);
		}
		*/
		
		/*
		Source src = new MappedFileSource(name);
		ControllableRunner runner = new ControllableRunner(src).runWith(this);
		while(true) {
			runner.getLastTick();
			Thread.sleep(500);
		}
		*/
	}
	
	
	@OnCombatLogEntry
	public void onCombatLogEntry(Context ctx, CombatLogEntry cle) {
		System.out.println(cle.getAttackerName() + " attacked someone!");
	}
	
	@OnTickStart
	public void beginTick(Context ctx, boolean synthetic) {
		int tick = ctx.getTick();
		System.out.println("Tick: " + tick);
		Entity gp = ctx.getProcessor(Entities.class).getByDtName("CDOTAGamerulesProxy");
		if(gp == null) { return; }
		String prop = "m_pGameRules.m_fGameTime";
		if(gp.hasProperties(prop)) {
			float time = gp.getProperty(prop);
			System.out.println("\t" + (time / 60f));
			//System.out.println("\t" + gp.getProperty(prop));
		}
		/*
		Entity ck = ctx.getProcessor(Entities.class).getByDtName("CDOTA_Unit_Hero_Invoker");
		if(ck == null) { return; }
		List<Integer> ability_handles = new Vector<Integer>();
		for(int i=0;i<17;i++) {
			String prop = String.format("m_hAbilities.%04d",i);
			if(ck.hasProperties(prop) == false) {
				System.out.println("\t\tNo ability " + i);
			} else {
				Integer ah = ck.getProperty(String.format("m_hAbilities.%04d",i));
				//System.out.println("\t" + ah);
				Entity a = ctx.getProcessor(Entities.class).getByHandle(ah);
				System.out.println("\t" + ah + " = " + a.getDtClass().getDtName());
			}
		}
		*/
		

	}
	
	public static void main(String[] argv) throws Exception {
		new stream_test(argv[0]);
	}
}
