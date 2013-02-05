package transactionaFileIO;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{
	private static final long serialVersionUID = 9L;
	private String path;
	private long offset;
	

	public TransactionalFileOutputStream(String path) {
		this.path = path;
		this.offset = 0;
	}

	@Override
	public void write(int b) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(path, "rw");
		raf.seek(offset);
		raf.write(b);
		offset = raf.getFilePointer();
		raf.close();
	}
	
	//testing function
	public static void main(String[] args) {
		TransactionalFileOutputStream tOut = new TransactionalFileOutputStream("asdf.txt");
		PrintStream out = new PrintStream(tOut);
		out.println("candyman was here");
		
		TransactionalFileInputStream tIn = new TransactionalFileInputStream("asdf.txt");
		DataInputStream in = new DataInputStream(tIn);
		/*try {
			System.out.println(in.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

}
