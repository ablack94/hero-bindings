import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import vconsole2.ConsolePacket;
import vconsole2.VConsole2;
import vconsole2.VConsoleListener;


public class record_test implements VConsoleListener {
	public static void main(String[] argv) throws IOException, InterruptedException {
		record_test r = new record_test();
	}
	
	public record_test() throws UnknownHostException, IOException, InterruptedException {
		VConsole2 console = new VConsole2();
		console.addListener(this);
		console.start();
		
		Thread.sleep(5000);
		console.send(ConsolePacket.buildCommand("sv_cheats 1"));
		
		console.send(ConsolePacket.buildCommand("record test12345678910"));
		Thread.sleep(10000);
		console.send(ConsolePacket.buildCommand("stop"));
	}

	@Override
	public void onPacketReceived(ConsolePacket packet) {
		System.out.println(new String(packet.getPayload(), StandardCharsets.UTF_8));
	}
}
