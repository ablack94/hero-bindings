package vconsole2;

import java.util.concurrent.Semaphore;

public class SendResult {
	
	protected ConsolePacket packet;
	protected Semaphore barrier;
	protected volatile boolean success;
	
	public ConsolePacket getPacket() { return packet; }
	public boolean isSuccess() { return success; }
	
	public void setSuccess(boolean success) { this.success = success; }
	
	public SendResult(ConsolePacket packet) {
		this.packet = packet;
		this.barrier = new Semaphore(1);
		this.success = false;
	}
	
	public void complete() {
		complete(true);
	}
	
	public void complete(boolean success) {
		setSuccess(success);
		this.barrier.release();
	}
	
	public boolean waitOn() throws InterruptedException { 
		this.barrier.acquire();
		return this.success;
	}
	
}
