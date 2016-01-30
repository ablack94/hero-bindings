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
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VConsole2 {
	private static Logger log = Logger.getLogger(VConsole2.class.getName());
	
	private interface State {
		public boolean connect() throws IllegalStateException;
		public void disconnect() throws IllegalStateException, IOException;
		public ConsolePacket next() throws IllegalStateException, InterruptedException;
		public SendResult send(ConsolePacket packet) throws IllegalStateException;
	}
	
	private class DisconnectedState implements State {
		public boolean connect() throws IllegalStateException {
			log.fine("Connecting");
			try {
				conn = new Socket(InetAddress.getLocalHost(), port);
				_input = conn.getInputStream();
				_output = conn.getOutputStream();
				thread_read = new Thread(new Runnable() {
					public void run() {
						run_read();
					}
				});
				thread_write = new Thread(new Runnable() {
					public void run() {
						run_write();
					}
				});
				thread_read.start();
				thread_write.start();
				setState(new ConnectedState());
			} catch(IOException e) {
				if(conn != null) {
					try {
						conn.close();
					} catch(Exception e2) { }
					try {
						_input.close();
					} catch(Exception e2) { }
					try {
						_output.close();
					} catch(Exception e2) { }
					conn = null;
					_input = null;
					_output = null;
					thread_read = null;
					thread_write = null;
				}
				return false;
			}
			return true;
		}
		public void disconnect() throws IllegalStateException, IOException {
			throw new IllegalStateException("Already disconnected.");
		}
		public ConsolePacket next() {
			throw new IllegalStateException("No connection, can't read.");
		}
		public SendResult send(ConsolePacket packet) {
			throw new IllegalStateException("No connection, can't send.");
		}		
	}
	
	private class ConnectedState implements State {
		public boolean connect() throws IllegalStateException {
			throw new IllegalStateException("Already connected.");
		}
		public void disconnect() throws IllegalStateException, IOException {
			log.fine("Disconnecting");
			try {
				conn.close();
			} catch(IOException e) {
				log.log(Level.WARNING, "Problem closing socket.", e);
			} finally {
				conn = null;
				_input = null;
				_output = null;
			}
			thread_read.interrupt();
			thread_write.interrupt();
			try {
				if(Thread.currentThread() != thread_read && Thread.currentThread() != thread_write) {
					Thread.sleep(5000);
					if(thread_read.isAlive()) {
						log.warning("Read thread didn't terminate in an appropriate amount of time. Most likely a bug, could have performance immplications.");
					}
					if(thread_write.isAlive()) {
						log.warning("Write thread didn't terminate in an appropriate amount of time. Most likely a bug, could have performance implications.");
					}
				}
			} catch (InterruptedException e) {
				log.warning("Interrupted while disconnecting, thread state not guaranteed.");
			} finally {
				conn = null;
				thread_read = null;
				thread_write = null;
				read_queue.clear();
				write_queue.clear();
				setState(new DisconnectedState());
			}
		}
		public ConsolePacket next() throws InterruptedException {
			return read_queue.take();
		}
		public SendResult send(ConsolePacket packet) {
			PacketToWrite ptw = new PacketToWrite(packet);
			write_queue.add(ptw);
			return ptw.getResult();
		}		
	}
	
	public static final int DEFAULT_PORT = 29000;
	
	/* Members */
	private State state;
	private ReentrantLock state_lock;
	private int timeout; // ms
	private int port;
	private Socket conn;
	private Thread thread_read;
	private Thread thread_write;
	private ReentrantLock write_lock;
	
	private InputStream _input;
	private OutputStream _output;
	
	private LinkedBlockingQueue<VConsoleListener> listeners;
	private LinkedBlockingQueue<ConsolePacket> read_queue;
	private LinkedBlockingQueue<PacketToWrite> write_queue;
	
	/* Accessors/Mutators */
	
	/* Constructor(s) */
	public VConsole2() {
		this(DEFAULT_PORT);
	}
	
	public VConsole2(int port) {
		this.state = new DisconnectedState();
		this.state_lock = new ReentrantLock();
		
		this.timeout = 2000;
		this.port = port;
		this.conn = null;
		this.thread_read = null;
		this.thread_write = null;
		this.listeners = new LinkedBlockingQueue<VConsoleListener>();
		this.read_queue = new LinkedBlockingQueue<ConsolePacket>();
		this.write_queue = new LinkedBlockingQueue<PacketToWrite>();
		this.write_lock = new ReentrantLock();
		
		this._input = null;
		this._output = null;
	}
	
	protected void listenersOnPacketReceived(ConsolePacket packet) {
		for(VConsoleListener listener : this.listeners) {
			try {
				listener.onPacketReceived(packet);
			} catch(Exception e) {
				log.log(Level.WARNING, "Error in onPacketReceived listener!", e);
			}
		}
	}
	
	protected void listenersOnConnected() {
		for(VConsoleListener listener : this.listeners) {
			try {
				listener.onConnected();
			} catch(Exception e) {
				log.log(Level.WARNING, "Error in onConnected listener!", e);
			}
		}
	}
	
	protected void listenersOnDisconnect() {
		for(VConsoleListener listener : this.listeners) {
			try {
				listener.onDisconnect();
			} catch(Exception e) {
				log.log(Level.WARNING, "Error in onDisconnected listener!", e);
			}
		}
	}
		
	public void addListener(VConsoleListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(VConsoleListener listener) {
		while(this.listeners.remove(listener)); // remove all instances of 'listener'
	}
		
	public boolean connect() throws IllegalStateException {
		boolean result;
		this.state_lock.lock();
		try {
			result = this.state.connect();
			listenersOnConnected();
		} finally {
			this.state_lock.unlock();
		}
		return result;
		
	}
	public void disconnect() throws IllegalStateException, IOException {
		this.state_lock.lock();
		try {
			this.state.disconnect();
			listenersOnDisconnect();
		} finally {
			this.state_lock.unlock();
		}
	}
	public ConsolePacket next() throws IllegalStateException, InterruptedException {
		this.state_lock.lock();
		try {
			return this.state.next();
		} finally {
			this.state_lock.unlock();
		}
	}
	public SendResult send(ConsolePacket packet) throws IllegalStateException {
		this.state_lock.lock();
		try {
			return this.state.send(packet);
		} finally {
			this.state_lock.unlock();
		}
	}
	
	protected void setState(State s) {
		this.state_lock.lock();
		try {
			this.state = s;
		} finally {
			this.state_lock.unlock();
		}
	}
	
	private void run_write() {
		PacketToWrite temp = null;
		while(Thread.interrupted() == false) {
			try {
				temp = write_queue.take();
				_output.write(temp.getPacket().serialize());
				_output.flush();
				temp.getResult().complete();
			} catch(InterruptedException e) {
				break;
			} catch(IOException e) {
				if(temp != null) {
					temp.getResult().setSuccess(false);
				}
			} finally {
				temp = null;
			}
		}
		
		try {
			disconnect();
		} catch(Exception e) {
			// Don't need to do anything, don't care really
		}
		log.log(Level.FINEST, "Write thread terminated.");
	}
	
	private void run_read() {
		boolean closing = false;
		byte[] header_buf = new byte[12];
		int count;
		byte[] payload_buf;
		while(true) {
			if(Thread.interrupted()) {
				closing = true;
				break;
			}
			try {
				for(int i=0;i<12;i+=_input.read(header_buf));
			} catch (IOException e) {
				if(Thread.interrupted() == false) {
					log.log(Level.WARNING, "Error reading header!", e);
				} else {
					closing = true;
				}
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
				for(int i=0;i<payload_length;i+=_input.read(payload_buf));
			} catch(IOException e) {
				log.log(Level.WARNING, "Error reading payload!", e);
				break;
			}
			// Build packet
			ConsolePacket p = new ConsolePacket(command, payload_buf);
			// Notify listeners
			listenersOnPacketReceived(p);
		}
		
		try {
			if(closing == false) {
				disconnect();
			}
		} catch(Exception e) {
			// Don't need to do anything, don't care really
		}
		log.log(Level.FINEST, "Read thread terminated.");
	}
	
	private class PacketToWrite {
		private SendResult result;
		private ConsolePacket packet;
		public SendResult getResult() { return result; }
		public ConsolePacket getPacket() { return packet; }
		
		public PacketToWrite(ConsolePacket packet) {
			this.result = new SendResult(packet);
			this.packet = packet;
		}
	}
	
}
