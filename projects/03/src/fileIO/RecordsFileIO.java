/**
 * RecordFileIO handles IO requests to the file of records.
 */

package fileio;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import util.Util;

import api.ByteArrayWritable;
import api.StringWritable;
import api.Writable;

public class RecordsFileIO {
	private File file;
	private boolean isReadFile;
	private int readRecordId;
	private int writeRecordId;
	private RandomAccessFile raf;
	private ObjectInputStream readObjectStream;
	private ObjectOutputStream writeObjectStream;
	
	/**
	 * Constructor for RecordsFileIO.
	 * RecordsFileIO can only read or write, not both.
	 * @param path
	 * @param createIfDoesntExist
	 * @param isReadFile
	 */
	public RecordsFileIO(String path, boolean createIfDoesntExist, boolean isReadFile) {
		this.file = new File(path);
		
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
	private void setIsReadFile(boolean newIsReadFile) {
		isReadFile = newIsReadFile;
		if(isReadFile) {
			readRecordId = 0;
			if(raf != null) {
				try {
					raf.seek(0);
				} catch (IOException e) {
					// TODO: remove debugging
					System.out.println("RecordsFileIO.setIsReadFile: unable to set raf file pointer to start at: " + getPath());
					e.printStackTrace();
				}
			}
		} else {
			writeRecordId = 0;
			if(raf != null) {
				try {
					raf.seek(raf.length());
				} catch (IOException e) { 
					// TODO: remove debugging
					System.out.println("RecordsFileIO.setIsReadFile: unable to set raf file pointer to end at: " + getPath());
					e.printStackTrace();
				}
			}
			writeObjectStream = openWriteStream();
		}
	}
	
	private RandomAccessFile open() {
		if(file != null) {
			try {
				return new RandomAccessFile(file, "rw");
			} catch (FileNotFoundException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.open: unable to open raf at: " + getPath());
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
				return new ObjectOutputStream(new FileOutputStream(getPath()));
			} catch (FileNotFoundException e) {
				System.out.println("tFile.openWriteStream: unable to create FileOutputStream at: " + getPath());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("tFile.openWriteStream: unable to create ObjectOutputStream at: " + getPath());
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
				return new ObjectInputStream(new FileInputStream(getPath()));
			} catch (FileNotFoundException e) {
				// TODO remove debugging
				System.out.println("tFile.openReadStream: unable to create FileInputStream: " + getPath());
				e.printStackTrace();
			} catch (IOException e) {
				// TODO remove debugging
				System.out.println("tFile.openReadStream: unable to create ObjectInputStream: " + getPath());
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
				// TODO: remove debugging
				System.out.println("RecordsFileIO.close: unable to close raf at: " + getPath());
				e.printStackTrace();
			}
			raf = null;
		}
		
		file = null;
	}
	
	/**
	 * Reads the next record.
	 * @param delimiter
	 * @return
	 */
	public Record readNextRecord(String delimiter) {
		Record r = null;
		if(isReadFile) {
			if(readObjectStream == null) {
				readObjectStream = openReadStream();
			}
			
			try {
				r = (Record) readObjectStream.readObject();
				readObjectStream.skipBytes(delimiter.length());
			} catch (EOFException e) {
				return null;
			} catch (ClassNotFoundException e1) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.readNextRecord: failed to read object at: " + getPath());
				e1.printStackTrace();
			} catch (IOException e1) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.readNextRecord: failed to read object or skip bytes at: " + getPath());
				e1.printStackTrace();
			}
			
			readRecordId++;
		}
		return r;
	}
	
	/**
	 * Reads the next String.
	 * @param delimiter
	 * @return
	 */
	public Record readNextString(String delimiter) {
		Record r = null;
		if(isReadFile && raf != null) {
			
			StringBuilder s = new StringBuilder();
			
			boolean moreToRead = true;
			while(moreToRead) {
				try {
					char c = (char) raf.readByte();
					s.append(c);
					//System.out.println("found: " + c);
				} catch(EOFException e) {
					moreToRead = false;
					if(s.length() == 0) {
						// if we haven't ready anything and we get an EOF, this means there was no record
						return null;
					}
				} catch (IOException e) {
					//TODO: remove debugging
					System.out.println("RecordsFileIO.readNextString: failed to read byte at: " + getPath());
					e.printStackTrace();
				}
				
				int delimiterIndex = s.indexOf(delimiter);
				
				if(delimiterIndex != -1) {
					moreToRead = false;
					s.delete(delimiterIndex, s.length());
				}
			}
			//System.out.println("readNextString: " + s.toString());
			Writable[] values = new Writable[] {new StringWritable(s.toString())};
			r = new Record(new StringWritable(getPath() + "_" + readRecordId), values);
			readRecordId++;
		}
		
		return r;
	}
	
	/**
	 * Reads the next record as an array of bytes.
	 * @param delimiter
	 * @return
	 */
	public Record readNextBytes(String delimiter) {
		Record r = null;
		if(isReadFile && raf != null) {
			
			ArrayList<Byte> s = new ArrayList<Byte>();
			byte[] delimiterBArr = (byte[]) delimiter.getBytes();
			
			boolean moreToRead = true;
			
			int delimiterMatches = 0;
			while(moreToRead) {
				try {
					byte b = raf.readByte();
					s.add(b);
					//System.out.println("found: "+(char) b);
					if(delimiterBArr[delimiterMatches] == b) {
						delimiterMatches++;
					}
				} catch(EOFException e) {
					moreToRead = false;
					if(s.size() == 0) {
						// if we haven't ready anything and we get an EOF, this means there was no record
						return null;
					}
				} catch (IOException e) {
					//TODO: remove debugging
					System.out.println("RecordsFileIO.readNextBytes: failed to read byte at: " + getPath());
					e.printStackTrace();
				}
				
				
				
				if(delimiterMatches == delimiterBArr.length) {
					moreToRead = false;
					for(int d = 0; d < delimiterBArr.length; d++) {
						s.remove(s.size()-1);
					}
				}
			}
			
			Byte[] sBytes = new Byte[s.size()];
			sBytes = s.toArray(sBytes);
			//System.out.println("readBytes: " + new String(Util.tobyteArray(sBytes)));
			
			Writable[] values = new Writable[] {new ByteArrayWritable(sBytes)};
			r = new Record(new StringWritable(getPath() + "_" + readRecordId), values);
			readRecordId++;
		}
		return r;
	}
	
	/**
	 * Writes a record as delimiter + record to the end of the file.
	 * @param record
	 * @param delimiter
	 */
	public void writeNextRecord(Record record, String delimiter) {
		if(!isReadFile) {
			if(writeObjectStream == null) {
				writeObjectStream = openWriteStream();
			}
			
			try {
				String del = (writeRecordId == 0) ? "" : delimiter;
				writeObjectStream.writeBytes(del);
				writeObjectStream.writeObject(record);
				writeRecordId++;
			} catch (IOException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.writeNextRecord(): error accessing/writing file at: " + getPath());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Writes a record as delimiter + recordStr to the end of the file.
	 * @param record
	 * @param delimiter
	 */
	public void writeNextString(String recordStr, String delimiter) {
		if(!isReadFile && raf != null) {
			try {
				String del = (writeRecordId == 0) ? "" : delimiter;
				raf.writeBytes(del + recordStr);
				writeRecordId++;
			} catch (IOException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.writeNextString(): error accessing/writing file at: " + getPath());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Writes a record as delimiter + recordStr to the end of the file.
	 * @param record
	 * @param delimiter
	 */
	public void writeNextBytes(Byte[] recordBytes, String delimiter) {
		if(!isReadFile && raf != null) {
			try {
				String del = (writeRecordId == 0) ? "" : delimiter;
				raf.writeBytes(del);
				raf.write(Util.tobyteArray(recordBytes));
				writeRecordId++;
			} catch (IOException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.writeNextString(): error accessing/writing file at: " + getPath());
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
	// TODO: this has the same comment as partitionRecords() - change it so that the difference is clear
	public void partitionData(String[] newPaths, int recordsPerPartition, String readDelimiter, String writeDelimiter) {
		if(isReadFile) {
			boolean moreToRead = true;
			for(int p = 0; moreToRead && p < newPaths.length; p++) {
				// TODO: lets hope the file doesn't already exist or bad shit happens
				// TODO: fix this ASAP!!!
				RecordsFileIO newRecordsFile = new RecordsFileIO(newPaths[p], true, false);
				for(int r = 0; moreToRead && r < recordsPerPartition; r++) {
					Record record = readNextBytes(readDelimiter);
					if(record != null) {
						ByteArrayWritable bytes = (ByteArrayWritable) record.getValues()[0];
						newRecordsFile.writeNextBytes(bytes.getValue(), writeDelimiter);
					} else {
						moreToRead = false;
					}
				}
				
				newRecordsFile.close();
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
	public void partitionRecords(String[] newPaths, int recordsPerPartition, String readDelimiter, String writeDelimiter) {
		if(isReadFile) {
			boolean moreToRead = true;
			for(int p = 0; moreToRead && p < newPaths.length; p++) {
				// TODO: lets hope the file doesn't already exist or bad shit happens
				RecordsFileIO newRecordsFile = new RecordsFileIO(newPaths[p], true, false);
				for(int r = 0; moreToRead && r < recordsPerPartition; r++) {
					Record record = readNextRecord(readDelimiter);
					if(record != null) {
						newRecordsFile.writeNextRecord(record, writeDelimiter);
					} else {
						moreToRead = false;
					}
				}
				
				newRecordsFile.close();
			}
		}
	}
	
	/**
	 * Closes and deletes the file.
	 */
	public void delete() {
		if(file != null) {
			file.delete();
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
