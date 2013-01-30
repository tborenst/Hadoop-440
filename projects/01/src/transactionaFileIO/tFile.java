package transactionaFileIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class tFile {
	private File file;
	
	public tFile(String path) {
		this.file = new File(path);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("tFile.tFile(): failed to create file: "+path);
				e.printStackTrace();
			}
		}
	}

	/**
	 * RandomAccessFile open(void):
	 * Creates a RandomAccessFile from File file with read-write access.
	 */
	private RandomAccessFile open() {
		if(file != null) {
			try {			
				return new RandomAccessFile(file, "rw");
			} catch (FileNotFoundException e) {
				System.out.println("tFile.open(): couldn't open file: "+file.getPath());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * boolean move(String):
	 * Moves file to the new specified destination.
	 */
	public boolean move(String destPath) {
		boolean success = false;
		if(file != null) {
			File dest = new File(destPath);
			success = file.renameTo(dest);
			if(success) {file = dest;}
		}
		return success;
	}
	
	/**
	 * boolean delete(void):
	 * Returns File.delete() and sets file to null if delete was successful.
	 */
	public boolean delete() {
		boolean success = false;
		if(file != null) {
			success = file.delete();
			if(success) {file = null;}
		}
		return success;
	}
	
	/**
	 * String getPath(void):
	 * Returns File.getPath().
	 */
	public String getPath() {
		String path = "";
		if(file != null) {
			path = file.getPath();
		}
		return path;
	}
	
	/**
	 * boolean exists(void):
	 * Returns File.exists().
	 */
	public boolean exists() {
		return file != null && file.exists();
	}
	
	/**
	 * void write(String):
	 * Writes from the end of the file and then closes the connection.
	 */
	public void write(String s) {
		long fileLength = 0;
		RandomAccessFile raf = open();
		if(raf != null) {
			try {
				fileLength = raf.length();
				raf.seek(fileLength);
				raf.writeBytes(s);
				raf.close();
			} catch (IOException e) {
				System.out.println("tFile.write(): error accessing/writing file: "+file.getPath());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * void writeTo(String, long):
	 * Writes from the specified location and then closes the connection.
	 */
	public void writeTo(String s, long location) {
		RandomAccessFile raf = open();
		if(raf != null) {
			try {
				raf.seek(location);
				raf.writeBytes(s);
				raf.close();
			} catch (IOException e) {
				System.out.println("tFile.writeTo(): error accessing/writing file: "+file.getPath());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * String read(void):
	 * Reads from beginning of file and then closes the connection.
	 */
	public String read() {
		RandomAccessFile raf = open();
		if(raf != null) {
			try {
				long len = raf.length();
				StringBuilder s = new StringBuilder((int) len);
				for (long i = 0; i < len; i++) {
					s.append((char) raf.readByte());
				}
				
				raf.close();
				return s.toString();
			} catch (IOException e) {
				System.out.println("tFile.read(): error reading file: "+file.getPath());
				e.printStackTrace();
			}
		}
		return "";
	}
	
	//testing
	public static void main(String[] args) {
		tFile test = new tFile("./asdf.txt");
		tFile test2 = new tFile("./asdf.txt");
		test.writeTo("candyman wrote this 2", 0);
		System.out.println(test2.read());
		test2.writeTo("candyman wrote this 1", 0);
		System.out.println(test2.read());
		System.out.println(test.read());
		
	}
	
}