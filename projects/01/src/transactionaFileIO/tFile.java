/**
 * The tFile class takes in a path String and creates a new File object. It provides several public methods
 * to control the file similar to the File class. tFile immediately closes the file after read/write sessions.
 */

package transactionaFileIO;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class tFile implements Serializable{
	private File file;
	private static final long serialVersionUID = 3L;
	
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
	 * ObjectOutputStream openWriteStream(void):
	 * Creates a ObjectOutputStream from File file.
	 */
	private ObjectOutputStream openWriteStream() {
		if(file != null) {			
			try {
				return new ObjectOutputStream(new FileOutputStream(file.getPath()));
			} catch (FileNotFoundException e) {
				System.out.println("tFile.openWriteStream: unable to create FileOutputStream: "+file.getPath());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("tFile.openWriteStream: unable to create ObjectOutputStream: "+file.getPath());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * ObjectInputStream openReadStream(void):
	 * Creates a ObjectInputStream from File file.
	 */
	private ObjectInputStream openReadStream() {
		if(file != null) {			
			try {
				return new ObjectInputStream(new FileInputStream(file.getPath()));
			} catch (FileNotFoundException e) {
				System.out.println("tFile.openReadStream: unable to create FileInputStream: "+file.getPath());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("tFile.openReadStream: unable to create ObjectInputStream: "+file.getPath());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * boolean move(String destPath):
	 * Moves file to the new specified destination.
	 * @param destPath
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
	 * void write(String s):
	 * Writes from the end of the file and then closes the connection.
	 * @param s
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
	 * void writeTo(String s, long location):
	 * Writes from the specified location and then closes the connection.
	 * @param s
	 * @param location
	 */
	public void writeTo(String s, long location) {
		RandomAccessFile raf = open();
		if(raf != null) {
			try {
				raf.seek(location);
				raf.writeBytes(s);
				raf.close();
			} catch (IOException e) {
				System.out.println("tFile.writeTo: error accessing/writing file: "+file.getPath());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * void writeImg(RenderedImage img, String format):
	 * Writes image to file in the given format.
	 * @param img
	 * @param format
	 */
	public void writeImg(RenderedImage img, String format) {
		try {
			ImageIO.write(img, format, file);
		} catch (IOException e) {
			System.out.println("tFile.writeImg: error writing "+format+" to "+file.getPath());
			e.printStackTrace();
		}
	}
	
	/**
	 * void writeObj(Object o):
	 * Serializes an Object o to file.
	 * @param o
	 */
	public void writeObj(Object o) {
		ObjectOutputStream s = openWriteStream();
		if(s != null) {
			try {
				s.writeObject(o);
				s.flush();
				s.close();
			} catch (IOException e) {
				System.out.println("tFile.writeObj: error writing object "+o.toString());
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
	
	/**
	 * Image readImg(void):
	 * Reads image from file.
	 */
	public BufferedImage readImg() {
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			System.out.println("tFile.readImg: failed to read image file: "+file.getPath());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Object readObj(void):
	 * Reads a serialized object from the file.
	 */
	public Object readObj() {
		ObjectInputStream s = openReadStream();
		if(s != null) {
			try {
				Object ret = s.readObject();				
				s.close();
				return ret;
			} catch (ClassNotFoundException e) {
				System.out.println("tFile.readObj: reading object from file: "+file.getPath());
				e.printStackTrace();
<<<<<<< HEAD
			} catch(IOException e){
=======
			} catch (IOException e) {
>>>>>>> 725d5804f7d286f24339fd80000dc0a2bc5cfefb
				System.out.println("tFile.readObj: reading object from file: "+file.getPath());
				e.printStackTrace();
			}
			
		}
		return null;
	}
	
	//testing
	public static void main(String[] args) {
		tFile test = new tFile("./obj.txt");
		tFile test2 = new tFile("./obj.txt");
		test.writeObj((Object) "candyman wrote this 2");
		System.out.println(test2.read());
		test2.writeObj((Object) "candyman wrote this 1");
		System.out.println(test2.read());
		System.out.println(test.read());
		
	}
	
}