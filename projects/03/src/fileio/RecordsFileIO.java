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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

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
		initialize(path, createIfDoesntExist, isReadFile);
	}
	
	/**
	 * Initializes the object.
	 * @param path
	 * @param createIfDoesntExist
	 * @param isReadFile
	 */
	private void initialize(String path, boolean createIfDoesntExist, boolean isReadFile) {
		file = new File(path);
		
		if(!file.exists() && createIfDoesntExist) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.RecordsFile(): failed to create file: " + path);
				e.printStackTrace();
			}
		}
		
		raf = open();
		setIsReadFile(isReadFile);
	}
	
	/**
	 * Getter for number of records read.
	 * @return
	 */
	public int getNumRecordsRead() {
		return readRecordId;
	}
	
	/**
	 * Getter for number of records written.
	 * @return
	 */
	public int getNumRecordsWritten() {
		return writeRecordId;
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
			closeStreams();
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
				System.out.println("RecordsFileIO.openReadStream: unable to create FileInputStream: " + getPath());
				e.printStackTrace();
			} catch (IOException e) {
				// TODO remove debugging
				System.out.println("RecordsFileIO.openReadStream: unable to create ObjectInputStream: " + getPath());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Closes the file and raf.
	 */
	public void close() {

		closeStreams();
		
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
		
		closeStreams();
		
		file = null;
	}
	
	/**
	 * Closes the writeStream and readStream.
	 */
	private void closeStreams() {
		if(readObjectStream != null) {
			try {
				readObjectStream.close();
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.closeStreams: unable to close readObjectStream at: " + getPath());
				e.printStackTrace();
			}
			
			readObjectStream = null;
		}
		
		if(writeObjectStream != null) {
			try {
				writeObjectStream.flush();
				writeObjectStream.close();
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.closeStreams: unable to flush & close writeObjectStream at: " + getPath());
				e.printStackTrace();
			}
			
			writeObjectStream = null;
		}
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
				try{
					readObjectStream = openReadStream();
				} catch (Exception e){
					System.out.println("FUCKAYOUWAHEL");
					e.printStackTrace();
				}
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
			byte[] delimiterBArr = delimiter.getBytes();
			
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
	 * Partitions the data and writes them into the files located at the paths in newPaths.
	 * Does not maintain order.
	 * @param newPaths - writes to
	 * @param readDelimiter
	 * @param writeDelimiter
	 * @returns int - The index of the last path written to, -1 if partitionData fails.
	 */
	public int dealData(String[] newPaths, String readDelimiter, String writeDelimiter) {
		int currPathIdx = -1;
		if(isReadFile) {
			RecordsFileIO[] newRecordsFiles = new RecordsFileIO[newPaths.length];
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f] = new RecordsFileIO(newPaths[f], true, false);
			}
			Record rec;
			for(int f = 0; (rec = readNextBytes(readDelimiter)) != null; f++) {
				f = f % newPaths.length; //make f wrap around
				currPathIdx = f;
				ByteArrayWritable bytes = (ByteArrayWritable) rec.getValues()[0];
				newRecordsFiles[f].writeNextBytes(bytes.getValue(), writeDelimiter);
			}
			
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f].close();
			}
		}
		return currPathIdx;
	}
	
	/**
	 * Partitions the data located at paths in dataPaths 
	 * and writes them into the files located at the paths in newRecordFilesPaths.
	 * Does not maintain order.
	 * @param dataPaths - reads from
	 * @param newDataFilesPaths - writes to
	 * @param readDelimiter
	 * @param writeDelimiter
	 * @returns int - The index of the last record written to, -1 if partitionData fails.
	 */
	public static int dealDataTo(String[] dataPaths, String[] newDataFilesPaths, String readDelimiter, String writeDelimiter) {
		int currRecordPathIdx = -1;
		
		RecordsFileIO[] newRecordsFiles = new RecordsFileIO[newDataFilesPaths.length];
		for(int f = 0; f < newDataFilesPaths.length; f++) {
			newRecordsFiles[f] = new RecordsFileIO(newDataFilesPaths[f], true, false);
		}
		
		
		for(int d = 0; d < dataPaths.length; d++) {
			RecordsFileIO dataFile = new RecordsFileIO(dataPaths[d], true, true);
			Record rec;
			for(int f = 0; (rec = dataFile.readNextBytes(readDelimiter)) != null;
					f = (f + 1) % newRecordsFiles.length) {
				currRecordPathIdx = d;
				ByteArrayWritable bytes = (ByteArrayWritable) rec.getValues()[0];
				newRecordsFiles[f].writeNextBytes(bytes.getValue(), writeDelimiter);
			}
			
			dataFile.close();
		}
		
		
		for(int f = 0; f < newRecordsFiles.length; f++) {
			newRecordsFiles[f].close();
		}
		
		return currRecordPathIdx;
	}
	
	
	/**
	 * Partitions the strings and writes them as Records into the files located at the paths in newPaths.
	 * Does not maintain order.
	 * @param newPaths - writes to
	 * @param readDelimiter
	 * @param writeDelimiter
	 * @returns int - The index of the last path written to, -1 if partitionData fails.
	 */
	public int dealStringsAsRecords(String[] newPaths, String readDelimiter, String writeDelimiter) {
		int currPathIdx = -1;
		if(isReadFile) {
			RecordsFileIO[] newRecordsFiles = new RecordsFileIO[newPaths.length];
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f] = new RecordsFileIO(newPaths[f], true, false);
			}
			Record rec;
			for(int f = 0; (rec = readNextString(readDelimiter)) != null; f++) {
				f = f % newPaths.length; //make f wrap around
				currPathIdx = f;
				newRecordsFiles[f].writeNextRecord(rec, writeDelimiter);
			}
			
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f].close();
			}
		}
		return currPathIdx;
	}
	
	
	
	/**
	 * Partitions the Strings located at paths in dataPaths 
	 * and writes them as Records into the files located at the paths in newRecordFilesPaths.
	 * Does not maintain order.
	 * @param dataPaths - reads from
	 * @param newDataFilesPaths - writes to
	 * @param readDelimiter
	 * @param writeDelimiter
	 * @returns int - The index of the last record written to, -1 if partitionData fails.
	 */
	public static int dealStringsAsRecordsTo(String[] dataPaths, String[] newDataFilesPaths, String readDelimiter, String writeDelimiter) {
		int currRecordPathIdx = -1;
		
		RecordsFileIO[] newRecordsFiles = new RecordsFileIO[newDataFilesPaths.length];
		for(int f = 0; f < newDataFilesPaths.length; f++) {
			newRecordsFiles[f] = new RecordsFileIO(newDataFilesPaths[f], true, false);
		}
		
		
		for(int d = 0; d < dataPaths.length; d++) {
			RecordsFileIO dataFile = new RecordsFileIO(dataPaths[d], true, true);
			Record rec;
			for(int f = 0; (rec = dataFile.readNextString(readDelimiter)) != null;
					f = (f + 1) % newRecordsFiles.length) {
				currRecordPathIdx = d;
				newRecordsFiles[f].writeNextRecord(rec, writeDelimiter);
			}
			
			dataFile.close();
		}
		
		
		for(int f = 0; f < newRecordsFiles.length; f++) {
			newRecordsFiles[f].close();
		}
		
		return currRecordPathIdx;
	}
	
	/**
	 * Partitions the records and writes them into the files located at the paths in newPaths.
	 * Does not maintain an order.
	 * @param newPaths - writes to
	 * @param readDelimiter
	 * @param writeDelimiter
	 * @return - The index of the last path written to, -1 if partitionData fails.
	 */
	public int dealRecords(String[] newPaths, String readDelimiter, String writeDelimiter) {
		int currPathIdx = -1;
		if(isReadFile) {
			RecordsFileIO[] newRecordsFiles = new RecordsFileIO[newPaths.length];
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f] = new RecordsFileIO(newPaths[f], true, false);
			}
			
			Record rec;
			for(int f = 0; (rec = readNextRecord(readDelimiter)) != null; 
					f = (f + 1) % newRecordsFiles.length) {
				currPathIdx = f;
				newRecordsFiles[f].writeNextRecord(rec, writeDelimiter);
			}
			
			for(int f = 0; f < newPaths.length; f++) {
				newRecordsFiles[f].close();
			}
		}
		
		return currPathIdx;
	}
	
	/**
	 * Partitions the data located at paths in recordsPaths 
	 * and writes them into the files located at the paths in newRecordFilesPaths.
	 * Does not maintain order.
	 * @param recordsPath - reads from
	 * @param newRecordFilesPaths - writes to
	 * @param readDelimiter
	 * @param writeDelimiter
	 * @returns int - The index of the last record written to, -1 if partitionData fails.
	 */
	public static int dealRecordsTo(String[] recordsPath, String[] newRecordFilesPaths, String readDelimiter, String writeDelimiter) {
		int currRecordPathIdx = -1;
		
		RecordsFileIO[] newRecordsFiles = new RecordsFileIO[newRecordFilesPaths.length];
		for(int f = 0; f < newRecordFilesPaths.length; f++) {
			newRecordsFiles[f] = new RecordsFileIO(newRecordFilesPaths[f], true, false);
		}
		
		for(int d = 0; d < recordsPath.length; d++) {
			RecordsFileIO recordsFile = new RecordsFileIO(recordsPath[d], true, true);
			Record rec;
			for(int f = 0; (rec = recordsFile.readNextRecord(readDelimiter)) != null;
					f = (f + 1) % newRecordsFiles.length) {
				currRecordPathIdx = d;
				newRecordsFiles[f].writeNextRecord(rec, writeDelimiter);
			}
			
			recordsFile.close();
		}
		
		for(int f = 0; f < newRecordsFiles.length; f++) {
			newRecordsFiles[f].close();
		}
		
		return currRecordPathIdx;
	}
	
	/**
	 * Partitions the records while maintaining the order of the records.
	 * @param destPaths - writes to
	 * @param numRecords
	 * @param readDelimiter
	 * @param writeDelimiter
	 */
	public void splitRecords(String[] destPaths, int numRecords, String readDelimiter, String writeDelimiter) {
		if(isReadFile) {
			int recordsPerPartition = (numRecords + destPaths.length - 1) / destPaths.length;
			
			for(int p = 0; p < destPaths.length; p++) {
				RecordsFileIO recsDest = new RecordsFileIO(destPaths[p], true, false);
				Record rec;
				for(int r = 0; r < recordsPerPartition && (rec = readNextRecord(readDelimiter)) != null; r++) {
					recsDest.writeNextRecord(rec, writeDelimiter);
				}
				
				recsDest.close();
			}
		}
	}
	
	/**
	 * Merges, sorts and then partitions the records from srcPaths to destPaths.
	 * @param srcPaths - from
	 * @param destPaths - to
	 * @param workingDir - temporary holding area for intermediary files
	 * @param readDelimiter
	 * @param writeDelimiter
	 * @throws DirectoryNotFoundException 
	 */
	public static void mergeSortRecords(String[] srcPaths, String[] destPaths, String workingDir, String readDelimiter, String writeDelimiter) throws DirectoryNotFoundException {
		if(!Util.isValidDirectory(workingDir)) {
			throw new DirectoryNotFoundException();
		}
		
		LinkedList<RecordsFileIO> recsQueue = new LinkedList<RecordsFileIO>();
		for(int p = 0; p < srcPaths.length; p++) {
			RecordsFileIO recs = new RecordsFileIO(srcPaths[p], true, true);
			recs.sortRecords(readDelimiter);
			recsQueue.add(recs);
		}
		
		int numPathsMerged = srcPaths.length;
		RecordsFileIO mergedRecs = null;
		while(recsQueue.size() >= 2) {
			String destPath = Util.generateRandomPath(workingDir, "/mergeSortIntermediary_", "txt");
			RecordsFileIO recs1 =  recsQueue.remove();
			recs1.setIsReadFile(true);
			RecordsFileIO recs2 = recsQueue.remove();
			recs2.setIsReadFile(true);
			
			mergedRecs = new RecordsFileIO(destPath, true, false);
			mergeRecordsTo(recs1, recs2, mergedRecs, readDelimiter, readDelimiter);
			
			numPathsMerged--;
			if(numPathsMerged < 0) {
				//System.out.println("deleting 1 " + recs1.getPath());
				recs1.close();
			} else {
				//System.out.println("closing 1 " + recs1.getPath());
				recs1.close();
			}
			//TODO: make it delete()
			numPathsMerged--;
			if(numPathsMerged < 0) {
				//System.out.println("deleting 2 " + recs2.getPath());
				recs2.close();
			} else {
				//System.out.println("closing 2 " + recs2.getPath());
				recs2.close();
			}
			
			recsQueue.add(mergedRecs);
		}
		
		mergedRecs = recsQueue.remove();
		
		if(mergedRecs != null) {
			mergedRecs.setIsReadFile(true);
			
			mergedRecs.splitRecords(destPaths, mergedRecs.getNumRecordsWritten(), readDelimiter, writeDelimiter);
			mergedRecs.delete();
		}
	}

	/**
	 * Merge the sorted records located at recs1 and recs2 into mergedRecs.
	 * recs1 and recs2 must be sorted.
	 * @param recs1 - sorted source
	 * @param recs2 - sorted source
	 * @param mergedRecs - destination
	 * @param readDelimiter
	 * @param writeDelimiter
	 */
	public static void mergeRecordsTo(RecordsFileIO recs1, RecordsFileIO recs2, RecordsFileIO mergedRecs, String readDelimiter, String writeDelimiter) {
		Record rec1 = recs1.readNextRecord(readDelimiter);
		Record rec2 = recs2.readNextRecord(readDelimiter); 
		
		while(rec1 != null || rec2 != null) {
			
			int diff = 1; //rec1 - rec2
			
			if(rec1 != null) {
				if(rec2 != null) {
					diff = rec1.compare(rec2);
				} else {
					diff = -1;
				}
			}
			
			Record writeRec = rec1;
			if(diff == 0) {
				writeRec.addValues(rec2.getValues());
				rec1 = recs1.readNextRecord(readDelimiter);
				rec2 = recs2.readNextRecord(readDelimiter);
			} else if(diff > 0) {
				writeRec = rec2;
				rec2 = recs2.readNextRecord(readDelimiter);
			} else {
				rec1 = recs1.readNextRecord(readDelimiter);
			}
			
			mergedRecs.writeNextRecord(writeRec, writeDelimiter);
		}
	}
	
	/**
	 * Destructively sort the records.
	 * RecordsFileIO must be in read mode.
	 * @param readDelimiter
	 * @warning Loads whole record into memory.
	 */
	public void sortRecords(String readDelimiter) {
		// TODO: load only keys into memory file ptrs??
		if(isReadFile) {
			ArrayList<Record> recs = new ArrayList<Record>();
			Record rec;
			while((rec = readNextRecord(readDelimiter)) != null) {
				recs.add(rec);
			}
			
			Collections.sort(recs, new Comparator<Record>() {
	
				@Override
				public int compare(Record o1, Record o2) {
					return o1.compare(o2);
				}
				
			});
			
			String path = getPath();
			delete();
			initialize(path, true, false);
			
			for(int r = 0; r < recs.size(); r++) {
				writeNextRecord(recs.get(r), readDelimiter);
			}
			
			setIsReadFile(true);
		}
	}
	
	/**
	 * Closes and deletes the file.
	 */
	public void delete() {
		if(file != null) {
			file.setWritable(true);
			boolean success = file.delete();
			if(!success) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.delete: failed to delete file at: " + getPath());
			}
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
