package vconsole2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class VConsole2 {
	public static final int DEFAULT_PORT = 29000;
	
	/* Members */
	private int port;
	private Socket conn;
	private Thread thread_read;
	private Thread thread_write;
	private ReentrantLock write_lock;
	
	private LinkedBlockingQueue<VConsoleListener> listeners;
	private LinkedBlockingQueue<ConsolePacket> write_queue;
	
	/* Accessors/Mutators */
	
	/* Constructor(s) */
	public VConsole2() {
		this(DEFAULT_PORT);
	}
	
	public VConsole2(int port) {
		this.port = port;
		this.conn = null;
		this.thread_read = null;
		this.thread_write = null;
		this.listeners = new LinkedBlockingQueue<VConsoleListener>();
		this.write_queue = new LinkedBlockingQueue<ConsolePacket>();
		this.write_lock = new ReentrantLock();
	}
	
	private void notifyListeners(ConsolePacket packet) {
		for(VConsoleListener listener : this.listeners) {
			try {
				listener.onPacketReceived(packet);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addListener(VConsoleListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(VConsoleListener listener) {
		while(this.listeners.remove(listener)); // remove all instances of 'listener'
	}
	
	public void start() throws UnknownHostException, IOException {
		this.thread_read = new Thread(new Runnable() {
			@Override
			public void run() {
				run_read();
			}
		});
		this.thread_write = new Thread(new Runnable() {
			@Override
			public void run() {
				run_write();
			}
		});
		this.thread_read.start();
		this.thread_write.start();
	}
	
	public void stop() {
		
	}
	
	public void send(ConsolePacket packet) throws IOException {
		write_queue.add(packet);
	}
	
	
	private void run_write() {
		ConsolePacket p = null;
		OutputStream os = null;
		while(Thread.interrupted() == false) {
			// Get a packet to send
			if(p == null) {
				try {
					p = write_queue.take();
				} catch(InterruptedException e) {
					return;
				}
			}
			// Send/retry the current packet
			if(os == null) {
				if(this.conn != null) {
					try {
						os = this.conn.getOutputStream();
					} catch(IOException e) {
						e.printStackTrace();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							return;
						}
					}
				}
			} else {
				try {
					System.out.println("Writing packet: " + new String(p.serialize(), StandardCharsets.UTF_8));
					os.write(p.serialize());
					byte[] _b = p.serialize();
					int[] _i = new int[_b.length];
					for(int i=0;i<_i.length;i++) {
						_i[i] = (int)(_b[i] & 0xff);
					}
					System.out.println(Arrays.toString(_i));
					os.flush();
					p = null;
				} catch(IOException e) {
					e.printStackTrace();
					os = null;
				}
			}
		}
	}
	
	private void run_read() {
		while(this.conn == null) {
			try {
				this.conn = new Socket(InetAddress.getLocalHost(), this.port);
			} catch(Exception e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					return;
				}
			}
		}
		System.out.println("Connected");
		/*
		try {
			this.send(ConsolePacket.buildCommand("clear"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		InputStream is = null;
		try {
			is = conn.getInputStream();
		} catch(IOException e) {
			System.err.println("Error opening socket input stream.");
			return;
		}
		byte[] header_buf = new byte[12];
		int count;
		byte[] payload_buf;
		while(Thread.interrupted() == false) {
			try {
				for(int i=0;i<12;i+=is.read(header_buf));
			} catch (IOException e) {
				System.err.println("Error reading header from socket.");
				e.printStackTrace();
				break;
			}
			// Parse the header
			String command = new String(new byte[] { header_buf[0], header_buf[1], header_buf[2], header_buf[3] });
			// version is at indicies 4,5,6,7, don't care about it
			int length = ((int)(header_buf[8] & 0xff) << 8) | ((int)(header_buf[9] & 0xff));
			// pipe handle is at indices 10, 11, don't care about it
			int payload_length = length - 12;
			// read payload
			payload_buf = new byte[payload_length];
			try {
				for(int i=0;i<payload_length;i+=is.read(payload_buf));
			} catch(IOException e) {
				System.err.println("Error reading payload from socket.");
				e.printStackTrace();
				break;
			}
			// Build packet
			ConsolePacket p = new ConsolePacket(command, payload_buf);
			// Notify listeners
			notifyListeners(p);
		}
	}
	
}
