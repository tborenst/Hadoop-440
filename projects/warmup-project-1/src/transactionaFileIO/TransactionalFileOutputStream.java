package transactionaFileIO;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{
	private static final long serialVersionUID = 9L;
	private String path;
	private long offset;
	private Boolean append;
	

	public TransactionalFileOutputStream(String path, Boolean append) throws IOException {
		this.path = path;
		File f = new File(path);
		if(!f.exists()) {f.createNewFile();}
		this.offset = 0;
		this.append = append;
	}

	@Override
	public void write(int b) throws IOException {
		File f = new File(path);
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		raf.seek(offset);
		raf.write(b);
		if(append) {offset = raf.getFilePointer();}
		raf.close();
	}
	
	//testing function
	public static void main(String[] args) throws IOException {
		TransactionalFileOutputStream tOut = new TransactionalFileOutputStream("asdf.txt", true);
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
