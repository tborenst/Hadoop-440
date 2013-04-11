/**
 * RecordFileIO handles io requests to the file of records.
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

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
	 * @param isReadFile - The RecordsFileIO can only read or write. 
	 * 		Set to true if RecordsFileIO reads only and writes only if false.
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
	
	/**
	 * Opens a RandomAccessFile of file.
	 * @return RandomAccessFile
	 */
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
	 * @return ObjectOutputStream
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
	 * @return ObjectInputStream
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
	 * Closes the file and raf.
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
	 * Reads and deserializes the next record.
	 * @param delimiter
	 * @return Record
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
	 * @return Record - The key is the path + record id.
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
	 * @return Record - The key is the path + record id.
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
				if(writeRecordId > 0) {
					writeObjectStream.writeBytes(delimiter);
				}
				
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
				if(writeRecordId > 0) {
					raf.writeBytes(delimiter);
				}
				
				raf.writeBytes(recordStr);
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
				if(writeRecordId > 0) {
					raf.writeBytes(delimiter);
				}
				
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
	 * @param readDelimiter
	 * @param writeDelimiter
	 */
				
	public void partitionData(String[] newPaths, String readDelimiter, String writeDelimiter) {
		if(isReadFile) {
			RecordsFileIO[] newRecordsFiles = new RecordsFileIO[newPaths.length];
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f] = new RecordsFileIO(newPaths[f], true, false);
			}
			Record rec;
			for(int f = 0; (rec = readNextBytes(readDelimiter)) != null; f++) {
				f = f % newPaths.length; //make f wrap around
				ByteArrayWritable bytes = (ByteArrayWritable) rec.getValues()[0];
				newRecordsFiles[f].writeNextBytes(bytes.getValue(), writeDelimiter);
			}
			
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f].close();
			}
		}
	}
	
	/**
	 * Partitions the records and writes them into the files located at the paths in newPaths.
	 * @param newPaths
	 * @param readDelimiter
	 * @param writeDelimiter
	 */
	public void partitionRecords(String[] newPaths, String readDelimiter, String writeDelimiter) {
		if(isReadFile) {
			RecordsFileIO[] newRecordsFiles = new RecordsFileIO[newPaths.length];
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f] = new RecordsFileIO(newPaths[f], true, false);
			}
			
			Record rec;
			for(int f = 0; (rec = readNextRecord(readDelimiter)) != null; f++) {
				f = f % newPaths.length; //make f wrap around
				
				newRecordsFiles[f].writeNextRecord(rec, writeDelimiter);
			}
			
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f].close();
			}
		}
	}
	
	/**
	 * Merges all the records in the files at oldPaths in to this file, in a sorted order.
	 * @param oldPaths
	 * @param readDelimiter
	 * @param writeDelimiter
	 */
	public void mergeRecords(String[] oldPaths, String readDelimiter, String writeDelimiter) {
		if(!isReadFile) {
			//System.out.println("Merging files into: " + getPath());
			HashMap<Writable, ArrayList<Writable>> mergedRecords = new HashMap<Writable, ArrayList<Writable>>();
			
			for(int p = 0; p < oldPaths.length; p++) {
				RecordsFileIO recsFile = new RecordsFileIO(oldPaths[p], true, true);
				Record rec;
				while((rec = recsFile.readNextRecord(readDelimiter)) != null) {
					Writable key = rec.getKey();
					//System.out.println("read: <" + key.getValue() + ", " + rec.getValues()[0].getValue() + ">");
					ArrayList<Writable> values = mergedRecords.get(key);
					if(values == null) {
						values = new ArrayList<Writable>();
						//System.out.println("Created new");
					}
					values.addAll(Arrays.asList(rec.getValues()));
					mergedRecords.put(key, values);
				}
			}
			
			Set<Writable> keySet = mergedRecords.keySet();
			Writable[] keys = keySet.toArray(new Writable[keySet.size()]);
			Arrays.sort(keys, new Comparator<Writable>() {

				@Override
				public int compare(Writable key1, Writable key2) {
					return key1.compare(key2);
				}
				
			});
			
			for(int k = 0; k < keys.length; k++) {
				Writable key = keys[k];
				ArrayList<Writable> valuesList = mergedRecords.get(key);
				if(valuesList != null) {
					Writable[] values = valuesList.toArray(new Writable[valuesList.size()]);
					Record rec = new Record(key, values);
					writeNextRecord(rec, writeDelimiter);
				}
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
	 * Returns true if the file exists.
	 */
	public boolean exists() {
		return file != null && file.exists();
	}
}