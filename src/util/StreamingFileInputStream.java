package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class StreamingFileInputStream extends InputStream  {

	private FileInputStream fis;
	private FileChannel fd;
	
	public StreamingFileInputStream(File f) throws FileNotFoundException {
		this.fis = new FileInputStream(f);
		this.fd = fis.getChannel();
	}
	
	@Override
	public int read() throws IOException {
		//System.out.println("Cur: " + fd.position() + "Size: " + fd.size());
		while(fd.position() >= fd.size()) ;
		return fis.read();
	}
	

}
