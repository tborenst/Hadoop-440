package transactionaFileIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable{
	private static final long serialVersionUID = 8L;
	private String path;
	private long offset;

	public TransactionalFileInputStream(String path) {
		this.path = path;
		this.offset = 0;
	}
	
	@Override
	public int read() throws IOException {
		RandomAccessFile raf = new RandomAccessFile(path, "r");
		raf.seek(offset);
		int readVal = raf.read();
		offset = raf.getFilePointer();
		raf.close();
		return readVal;
	}
}
