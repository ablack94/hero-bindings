import java.io.File;
import java.io.IOException;

import dota2.Hero;


public class DefaultHeroConfigGenerator {
	public static void main(String[] argv) throws IOException {
		File output_dir = new File(System.getProperty("user.home"), "DefaultHeroBindings");
		output_dir.mkdir();
		for(Hero h : Hero.values()) {
			File cur_file = new File(output_dir, String.format("hb_%s.cfg", h.name()));
			cur_file.createNewFile();
			System.out.println(h.toString());
		}
	}
}
