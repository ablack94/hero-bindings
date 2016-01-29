package vconsole2;

public interface VConsoleListener {
	public default void onConnected() {}
	public default void onDisconnect() {}
	public void onPacketReceived(ConsolePacket packet);
}
