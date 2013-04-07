/**
 * RecordFileIO handles IO requests to the file of records.
 */

package fileIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RecordsFileIO {
	private File file;
	private RandomAccessFile raf;
	private boolean isReadFile;
	private long filePointer;
	
	/**
	 * Constructor for RecordsFileIO.
	 * RecordsFileIO can only read or write, not both.
	 * @param path
	 * @param createIfDoesntExist
	 * @param isReadFile
	 */
	public RecordsFileIO(String path, boolean createIfDoesntExist, boolean isReadFile) {
		this.file = new File(path);
		this.isReadFile = isReadFile;
		this.filePointer = 0;
		
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
		} else if(raf != null){
			try {
				seek(raf.length());
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.RecordsFile(): failed to get raf.length(): " + getPath());
				e.printStackTrace();
			}
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
	 * Closes the file.
	 */
	public void close() {
		if(raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.close(): failed to close file" + file.getPath());
				e.printStackTrace();
			}
		}
		file = null;
		raf = null;
	}
	
	/**
	 * Reads the next record.
	 * @param delimiter
	 * @return
	 */
	public String readNextRecordStr(String delimiter) {
		if(raf != null && this.isReadFile) {
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
					
					if(delimiterIndex != -1) {
						return s.substring(0, delimiterIndex);
					} else {
						return s.toString();
					}
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
	 * @param recordStr
	 * @param delimiter
	 */
	public void writeNextRecordStr(String recordStr, String delimiter) {
		if(raf != null && !this.isReadFile) {
			try {
				seek(raf.length());
				raf.writeBytes(delimiter + recordStr);
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
					String recordStr = readNextRecordStr(readDelimiter);
					if(recordStr != null) {
						newRecordsFile.writeNextRecordStr(recordStr, tempWriteDelimiter);
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
	 * Moves file to the new specified destination.
	 * Keeps file pointer consistent
	 * @param destPath
	 */
	public void move(String destPath) {
		if(file != null) {
			File dest = new File(destPath);
			boolean success = file.renameTo(dest);
			if(success) {
				long originalFilePointer = 0;
				if(raf != null) {
					try {
						originalFilePointer = raf.getFilePointer();
						raf.close();
					} catch (IOException e) {
						// TODO: remove debugging
						System.out.println("RecordsFileIO.move(): failed to close original raf at: " + getPath());
						e.printStackTrace();
					}
				}
				
				file = dest;
				raf = open();
				try {
					raf.seek(originalFilePointer);
				} catch (IOException e) {
					// TODO: remove debugging
					System.out.println("RecordsFileIO.move(): failed to seek to original file pointer of: " +  originalFilePointer + ", at: " + getPath());
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Seeks to newFilePointer.
	 * @param newFilePointer
	 */
	public void seek(long newFilePointer) {
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
		if(raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.delete(): failed to close raf at: " + getPath());
				e.printStackTrace();
			}
			raf = null;
		}
		
		if(file != null) {
			file.delete();
			file = null;
		}
		
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
