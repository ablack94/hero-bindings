/* Andrew Black
 * January 25, 2015
 * Barebones vconsole2 TCP interface.
 */

package vconsole2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class ConsolePacket {
	public static final int VERSION = 53760; // seems to be the version used by dota2 as of 1/25/2016
	public static final int HEADER_SIZE = 12; // 12 byte headers right now
	
	private String type; // 4 characters representing the packet type
	private int length; // uint16, int in java
	//private long version; // uint32, long in java (don't care about this, not storing it right now)
	//private int pipe_handle; // uint16, int in java (don't care about this, not storing it, no clue what it even means)
	 
	private byte[] payload;
	
	/*
	 * Return the type of the packet, should always be 4 characters.
	*/
	public String getType() { return type; }
	
	/*
	 * Get the length of the packet in bytes, includes the size of the header.
	 */
	public int getLength() { return length; }
	
	/*
	 * Returns the length of the payload.
	 */
	public int getPayloadLength() { return length - HEADER_SIZE; }
	
	/*
	 * Get the byte array representing the payload.
	 */
	public byte[] getPayload() { return payload; }

	/* Constructor(s) */
	public ConsolePacket(String type, byte[] payload) {
		// Checks
		if(type == null) { throw new NullPointerException("Type can't be null!"); }
		if(type.length() != 4) { throw new IllegalArgumentException("Type must be exactly 4 characters!"); }
		if(payload == null) { throw new NullPointerException("Payload can't be null!"); }
		// Assignments
		this.type = type;
		this.payload = payload;
		this.length = HEADER_SIZE + payload.length;
	}
	
	/* Factory methods */
	
	/*
	 * Build a packet representing a command for the client (dota2) to execute.
	 * Note that \u0000 is the utf-16 hexadecimal escape i.e. \x00 in C++
	 */
	public static ConsolePacket buildCommand(String command) {
		return new ConsolePacket("CMND", command.concat("\u0000").getBytes(StandardCharsets.UTF_8));
	}
	
	/* Functions */
	
	/*
	 * Return a byte array suitable for transport over the wire.
	 */
	public byte[] serialize() {
		List<Byte> data = new Vector<Byte>();
		// 4 byte packet type
		for(Byte b : type.getBytes(StandardCharsets.UTF_8)) { data.add(b); }
		// 4 byte version number
		data.add((byte)0);
		data.add((byte)210);
		data.add((byte)0);
		data.add((byte)0);
		/*
		data.add((byte)((VERSION & 0xff000000) >> 24));
		data.add((byte)((VERSION & 0x00ff0000) >> 16));
		data.add((byte)((VERSION & 0x0000ff00) >>  8));
		data.add((byte)((VERSION & 0x000000ff) >>  0));
		*/
		// 2 byte little endian packet length 
		data.add((byte)((this.length & 0xff00) >> 8));
		data.add((byte)(this.length & 0xff));
		// 2 byte pipe handle, don't care about it
		data.add((byte)0);
		data.add((byte)0);
		// length - HEADER_SIZE payload
		for(Byte b : this.payload) { data.add(b); }
		// Create the output byte array, need to unbox the data list values individually
		byte[] output = new byte[data.size()];
		for(int i=0;i<output.length;i++) {
			output[i] = (byte)data.get(i);
		}
		return output;
	}
	
}
