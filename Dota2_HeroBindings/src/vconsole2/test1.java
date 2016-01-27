package vconsole2;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test1 implements VConsoleListener {
	
	private Pattern p;
	private Matcher m;
	
	public test1() throws UnknownHostException, IOException {
		p = Pattern.compile("(.*?)C:Gamerules: entering state '(\\w+)'(.*?)", Pattern.DOTALL);
		
		VConsole2 console = new VConsole2();
		console.addListener(this);
		console.start();
		
	}

	@Override
	public void onPacketReceived(ConsolePacket packet) {
		String data = new String(packet.getPayload(), StandardCharsets.UTF_8);
		m = p.matcher(data);
		if(m.matches()) {
			System.out.println("Game state: " + m.group(2));
		}
	}
	
	public static void main(String[] argv) throws Exception {
		test1 t = new test1();
		while(true);
	}
}
