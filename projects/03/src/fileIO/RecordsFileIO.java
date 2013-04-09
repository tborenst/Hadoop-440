/**
 * RecordFileIO handles IO requests to the file of records.
 */

package fileIO;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import util.Util;

import api.ByteArrayWritable;
import api.StringWritable;
import api.Writable;

public class RecordsFileIO {
	private File file;
	private boolean isReadFile;
	private int readRecordId;
	private int writeRecordId;
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
		this.readRecordId = 0;
		this.writeRecordId = 0;
		
		if(!this.file.exists() && createIfDoesntExist) {
			try {
				this.file.createNewFile();
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.RecordsFile(): failed to create file: " + path);
				e.printStackTrace();
			}
		}
		
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
			readRecordId = 0;
			closeStreams();
			readStream = openReadStream();
		} else {
			closeStreams();
			writeStream = openWriteStream();
		}
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
	 * Close writeStream and readStream.
	 */
	private void closeStreams() {
		if(writeStream != null) {
			try {
				writeStream.flush();
				writeStream.close();
			} catch (IOException e) {
				// TODO remove debugging
				System.out.println("tFile.closeStreams: unable to flush and/or close writeStream at: " + getPath());
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
		closeStreams();
		file = null;
	}
	
	/**
	 * Reads the next record.
	 * @param delimiter
	 * @return
	 */
	public Record readNextRecord(String delimiter) {
		Record r = null;
		if(isReadFile && readStream != null) {
			try {
				r = (Record) readStream.readObject();
				readStream.skipBytes(delimiter.length());
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
		if(isReadFile && readStream != null) {
			
			StringBuilder s = new StringBuilder();
			
			boolean moreToRead = false;
			while(moreToRead) {
				StringBuilder temp = new StringBuilder();
				for(int b = 0; moreToRead && b < delimiter.length(); b++) {
					try {
						temp.append((char) readStream.readByte());
					} catch(EOFException e) {
						moreToRead = false;
						if(s.length() + temp.length() == 0) {
							// if we haven't ready anything and we get an EOF, this means there was no record
							return null;
						}
					} catch (IOException e) {
						//TODO: remove debugging
						System.out.println("RecordsFileIO.readNextString: failed to read byte at: " + getPath());
						e.printStackTrace();
					}
				}
				
				if(temp.indexOf(delimiter) == -1) {
					s.append(temp);
				} else {
					moreToRead = false;
				}
			}
			
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
		if(isReadFile && readStream != null) {
			
			ArrayList<Byte> s = new ArrayList<Byte>();
			byte[] delimiterBArr = (byte[]) delimiter.getBytes();
			boolean moreToRead = false;
			
			while(moreToRead) {
				byte[] temp = new byte[delimiterBArr.length];
				for(int b = 0; moreToRead && b < delimiterBArr.length; b++) {
					try {
						temp[b] = readStream.readByte();
					} catch(EOFException e) {
						moreToRead = false;
						if(s.size() + b == 0) {
							// if we haven't ready anything and we get an EOF, this means there was no record
							return null;
						}
					} catch (IOException e) {
						//TODO: remove debugging
						System.out.println("RecordsFileIO.readNextByte: failed to read byte at: " + getPath());
						e.printStackTrace();
					}
				}
				
				if(!Arrays.equals(temp, delimiterBArr)) {
					for(int t = 0; t < temp.length; t++) {
						s.add((Byte) temp[t]);
					}
				} else {
					moreToRead = false;
				}
			}
			
			Byte[] sBytes = new Byte[s.size()];
			Writable[] values = new Writable[] {new ByteArrayWritable(s.toArray(sBytes))};
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
		if(!isReadFile && writeStream != null) {
			try {
				String del = (writeRecordId == 0) ? "" : delimiter;
				writeStream.writeBytes(del);
				writeStream.writeObject(record);
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
		if(!isReadFile && writeStream != null) {
			try {
				String del = (writeRecordId == 0) ? "" : delimiter;
				writeStream.writeBytes(del + recordStr);
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
		if(!isReadFile && writeStream != null) {
			try {
				String del = (writeRecordId == 0) ? "" : delimiter;
				writeStream.writeBytes(del);
				writeStream.write(Util.tobyteArray(recordBytes));
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
	public void partitionRecords(String[] newPaths, int recordsPerPartition, String readDelimiter, String writeDelimiter) {
		if(isReadFile) {
			boolean moreToRead = true;
			for(int p = 0; moreToRead && p < newPaths.length; p++) {
				// TODO: lets hope the file doesn't already exist or bad shit happens
				RecordsFileIO newRecordsFile = new RecordsFileIO(newPaths[p], true, false);
				String tempWriteDelimiter = "";
				for(int r = 0; moreToRead && r < recordsPerPartition; r++) {
					Record record = readNextRecord(readDelimiter);
					if(record != null) {
						//ByteArrayWritable bytes = (ByteArrayWritable) record.values[0];
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
