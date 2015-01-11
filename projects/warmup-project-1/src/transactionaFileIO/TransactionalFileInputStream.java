package transactionaFileIO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable{
	private static final long serialVersionUID = 8L;
	private String path;
	private long offset;

	public TransactionalFileInputStream(String path) throws IOException {
		this.path = path;
		File f = new File(path);
		if(!f.exists()) {f.createNewFile();}
		this.offset = (long) 0;
	}
	
	@Override
	public int read() throws IOException {
		File f = new File(path);
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		raf.seek(offset);
		int readVal = raf.read();
		offset = raf.getFilePointer();
		raf.close();
		return readVal;
	}
}
