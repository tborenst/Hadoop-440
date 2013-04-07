/**
 * RecordFileIO handles IO requests to the file of records.
 */

package fileIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

public class RecordsFileIO {
	private File file;
	private RandomAccessFile raf;
	private boolean isReadFile;
	private long filePointer;
	private int readRecordId;
	private ObjectOutputStream writeStream;
	private ObjectInputStream readStream;
	
	/**
	 * Constructor for RecordsFileIO.
	 * RecordsFileIO can only read or write, not both.
	 * @param path
	 * @param createIfDoesntExist
	 * @param isReadFile
	 */
	public RecordsFileIO(String path, boolean createIfDoesntExist, boolean isReadFile) {
		this.file = new File(path);
		this.filePointer = 0;
		this.readRecordId = 0;
		
		if(!this.file.exists() && createIfDoesntExist) {
			try {
				this.file.createNewFile();
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.RecordsFile(): failed to create file: " + path);
				e.printStackTrace();
			}
		}
		
		this.raf = open();
		setIsReadFile(isReadFile);
	}
	
	/**
	 * Changes the read or write setting for RecordsFileIO.
	 * And seeks to the front if newIsReadFile is true
	 * or to the end of newIsReadFile is false.
	 * @param newIsReadFile
	 */
	public void setIsReadFile(boolean newIsReadFile) {
		isReadFile = newIsReadFile;
		if(isReadFile) {
			seek(0);
			readRecordId = 0;
			closeStreams();
			readStream = openReadStream();
		} else {
			if(raf != null){
				try {
					seek(raf.length());
				} catch (IOException e) {
					// TODO: remove debugging
					System.out.println("RecordsFileIO.RecordsFile(): failed to get raf.length(): " + getPath());
					e.printStackTrace();
				}
			}
			
			closeStreams();
			writeStream = openWriteStream();
		}
	}
	
	/**
	 * Creates a RandomAccessFile from File file with read-write access.
	 */
	private RandomAccessFile open() {
		if(file != null) {
			try {
				String fileAccess = "rw";
				if(this.isReadFile) {fileAccess = "r";}
				return new RandomAccessFile(file, fileAccess);
			} catch (FileNotFoundException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.open(): couldn't open file: " + file.getPath());
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
				// TODO remove debugging
				System.out.println("tFile.openReadStream: unable to create FileInputStream: " + file.getPath());
				e.printStackTrace();
			} catch (IOException e) {
				// TODO remove debugging
				System.out.println("tFile.openReadStream: unable to create ObjectInputStream: " + file.getPath());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Close writeStream and readStream.
	 */
	private void closeStreams() {
		if(writeStream != null) {
			try {
				writeStream.flush();
				writeStream.close();
			} catch (IOException e) {
				// TODO remove debugging
				System.out.println("tFile.closeStreams: unable to flush and/or close writeStream at: " + file.getPath());
				e.printStackTrace();
			}
			writeStream = null;
		}
		
		if(readStream != null) {
			try {
				readStream.close();
			} catch (IOException e) {
				// TODO remove debugging
				System.out.println("tFile.closeStreams: unable to close readStream at: " + file.getPath());
				e.printStackTrace();
			}
			readStream = null;
		}
	}
	
	/**
	 * Closes the file.
	 */
	public void close() {
		if(raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.close(): failed to close raf at: " + file.getPath());
				e.printStackTrace();
			}
		}
		
		closeStreams();
		
		file = null;
		raf = null;
	}
	
	/**
	 * Reads the next record.
	 * @param delimiter
	 * @return
	 */
	public Record readNextRecord(String delimiter) {
		if(raf != null && this.isReadFile && readStream != null) {
			try {
				long len = raf.length()-1;
				if(this.filePointer < len) {
					StringBuilder s = new StringBuilder((int) len);
					int delimiterIndex = -1;
					for (; this.filePointer < len && delimiterIndex == -1; this.filePointer++) {
						s.append((char) raf.readByte());
						if(this.filePointer % delimiter.length() == 0) {
							delimiterIndex = s.indexOf(delimiter);
						}
					}
					
					// check if the delimiter is at the very end of file
					delimiterIndex = s.indexOf(delimiter);
					String valueStr = "";
					if(delimiterIndex != -1) {
						valueStr = s.substring(0, delimiterIndex);
					} else {
						valueStr = s.toString();
					}
					
					this.readRecordId++;
					return new Record(getPath()+"_"+this.readRecordId, new Object[] {valueStr});
				}
			} catch (IOException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.readNextRecord(): error reading file: " + getPath());
				e.printStackTrace();
			}
		}
		return null;
	}
		
	
	
	/**
	 * Writes a record as delimiter + recordStr to the end of the file.
	 * @param record
	 * @param delimiter
	 */
	public void writeNextRecord(Record record, String delimiter) {
		if(raf != null && !this.isReadFile && writeStream != null) {
			try {
				seek(raf.length());
				raf.writeBytes(delimiter);
				writeStream.writeObject(record);
			} catch (IOException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.writeNextRecordStr(): error accessing/writing file: "+file.getPath());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Partitions the records and writes them into the files located at the paths in newPaths.
	 * @param newPaths
	 * @param recordsPerPartition
	 * @param readDelimiter
	 * @param writeDelimiter
	 */
	public void partitionRecord(String[] newPaths, int recordsPerPartition, String readDelimiter, String writeDelimiter) {
		if(this.isReadFile) {
			boolean moreToRead = true;
			for(int p = 0; moreToRead && p < newPaths.length; p++) {
				//TODO: lets hope the file doesn't already exist or bad shit happens
				RecordsFileIO newRecordsFile = new RecordsFileIO(newPaths[p], true, false);
				String tempWriteDelimiter = "";
				for(int r = 0; moreToRead && r < recordsPerPartition; r++) {
					Record record = readNextRecord(readDelimiter);
					if(record != null) {
						newRecordsFile.writeNextRecord(record, tempWriteDelimiter);
						tempWriteDelimiter = writeDelimiter;
					} else {
						moreToRead = false;
					}
				}
				
				newRecordsFile.close();
			}
		}
	}
	
	/**
	 * Seeks to newFilePointer.
	 * @param newFilePointer
	 */
	private void seek(long newFilePointer) {
		if(raf != null) {
			try {
				raf.seek(newFilePointer);
				this.filePointer = newFilePointer;
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.delete(): failed to seek raf to: " + newFilePointer + "at: " + getPath());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Closes and deletes the file.
	 */
	public void delete() {
		if(file != null) {
			file.delete();
			file = null;
		}
		
		close();
		
	}
	
	/**
	 * Returns the path to the file.
	 * Returns null if file doesn't exist.
	 */
	public String getPath() {
		String path = null;
		if(file != null) {
			path = file.getPath();
		}
		return path;
	}
	
	/**
	 * Returns true if the file exists and false if the file does not exist.
	 */
	public boolean exists() {
		return file != null && file.exists();
	}
}
