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
	private String path;
	
	/**
	 * Constructor for RecordsFileIO.
	 * RecordsFileIO can only read or write, not both.
	 * @param path
	 * @param createIfDoesntExist
	 * @param isReadFile - The RecordsFileIO can only read or write. 
	 * 		Set to true if RecordsFileIO reads only and writes only if false.
	 * @throws IOException 
	 */
	public RecordsFileIO(String path, boolean createIfDoesntExist, boolean isReadFile) throws IOException {
		this.path = path;
		initialize(createIfDoesntExist, isReadFile);
	}
	
	/**
	 * Initializes the object.
	 * @param path
	 * @param createIfDoesntExist
	 * @param isReadFile
	 * @throws IOException 
	 */
	private void initialize(boolean createIfDoesntExist, boolean isReadFile) throws IOException {
		file = new File(path);
		
		if(!file.exists()) {
			if(createIfDoesntExist) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO: remove debugging
					System.out.println("RecordsFileIO.RecordsFile(): failed to create file: " + path);
					throw e;
				}
			} else {
				throw new FileNotFoundException();
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
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void setIsReadFile(boolean newIsReadFile) throws FileNotFoundException, IOException {
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
					throw e;
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
					throw e;
				}
			}
			writeObjectStream = openWriteStream();
		}
	}
	
	/**
	 * Opens a RandomAccessFile of file.
	 * @return RandomAccessFile
	 * @throws FileNotFoundException 
	 */
	private RandomAccessFile open() throws FileNotFoundException {
		if(file != null) {
			try {
				return new RandomAccessFile(file, "rw");
			} catch (FileNotFoundException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.open: unable to open raf at: " + getPath());
				throw e;
			}
		}
		
		return null;
	}
	
	/**
	 * ObjectOutputStream openWriteStream(void):
	 * Creates a ObjectOutputStream from File file.
	 * @return ObjectOutputStream
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	private ObjectOutputStream openWriteStream() throws FileNotFoundException, IOException {
		if(file != null) {			
			try {
				return new ObjectOutputStream(new FileOutputStream(getPath()));
			} catch (FileNotFoundException e) {
				System.out.println("tFile.openWriteStream: unable to create FileOutputStream at: " + getPath());
				throw e;
			} catch (IOException e) {
				System.out.println("tFile.openWriteStream: unable to create ObjectOutputStream at: " + getPath());
				throw e;
			}
		}
		return null;
	}
	
	/**
	 * ObjectInputStream openReadStream(void):
	 * Creates a ObjectInputStream from File file.
	 * @return ObjectInputStream
	 * @throws FileNotFoundException 
	 * @throws IOException
	 */
	private ObjectInputStream openReadStream() throws FileNotFoundException, IOException {
		if(file != null) {			
			try {
				return new ObjectInputStream(new FileInputStream(getPath()));
			} catch (FileNotFoundException e) {
				// TODO remove debugging
				System.out.println("RecordsFileIO.openReadStream: unable to create FileInputStream: " + getPath());
				throw e;
			} catch (IOException e) {
				// TODO remove debugging
				System.out.println("RecordsFileIO.openReadStream: unable to create ObjectInputStream: " + getPath());
				throw e;
			}
		}
		return null;
	}
	
	/**
	 * Closes the file and raf.
	 * @throws IOException 
	 */
	public void close() throws IOException {

		closeStreams();
		
		if(raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.close: unable to close raf at: " + getPath());
				throw e;
			}
			raf = null;
		}
		
		closeStreams();
		
		file = null;
	}
	
	/**
	 * Closes the writeStream and readStream.
	 * @throws IOException 
	 */
	private void closeStreams() throws IOException {
		if(readObjectStream != null) {
			try {
				readObjectStream.close();
			} catch (IOException e) {
				// TODO: remove debugging
				System.out.println("RecordsFileIO.closeStreams: unable to close readObjectStream at: " + getPath());
				throw e;
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
				throw e;
			}
			
			writeObjectStream = null;
		}
	}

	/**
	 * Reads and deserializes the next record.
	 * @param delimiter
	 * @return Record
	 * @throws EOFException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Record readNextRecord(String delimiter) throws EOFException, ClassNotFoundException, IOException {
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
			} catch (ClassNotFoundException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.readNextRecord: failed to read object at: " + getPath());
				throw e;
			} catch (IOException e) {
				//TODO: remove debugging
				System.out.println("RecordsFileIO.readNextRecord: failed to read object or skip bytes at: " + getPath());
				throw e;
			}
			
			readRecordId++;
		}
		return r;
	}
	
	/**
	 * Reads the next String.
	 * @param delimiter
	 * @return Record - The key is the path + record id.
	 * @throws IOException 
	 */
	public Record readNextString(String delimiter) throws IOException {
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
					throw e;
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
	 * @throws IOException 
	 */
	public Record readNextBytes(String delimiter) throws IOException {
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
					throw e;
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
	 * @throws IOException 
	 */
	public void writeNextRecord(Record record, String delimiter) throws IOException {
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
				throw e;
			}
		}
	}
	
	/**
	 * Writes a record as delimiter + recordStr to the end of the file.
	 * @param record
	 * @param delimiter
	 * @throws IOException 
	 */
	public void writeNextString(String recordStr, String delimiter) throws IOException {
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
				throw e;
			}
		}
	}
	
	/**
	 * Writes a record as delimiter + recordStr to the end of the file.
	 * @param record
	 * @param delimiter
	 * @throws IOException 
	 */
	public void writeNextBytes(Byte[] recordBytes, String delimiter) throws IOException {
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
				throw e;
			}
		}
	}
	
	/**
	 * Partitions the data and writes them into the files located at the paths in newPaths.
	 * Does not maintain order.
	 * @param newPaths - writes to
	 * @param readDelimiter
	 * @param writeDelimiter
	 * @throws InvalidArrayOfPathsException 
	 * @throws IOException 
	 * @returns int - The index of the last path written to, -1 if partitionData fails.
	 */
	public int dealData(String[] newPaths, String readDelimiter, String writeDelimiter) throws InvalidArrayOfPathsException, IOException {
		if(newPaths.length <= 0) {
			throw new InvalidArrayOfPathsException();
		}
		
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
	 * @throws InvalidArrayOfPathsException 
	 * @throws IOException 
	 * @returns int - The index of the last record written to, -1 if partitionData fails.
	 */
	public static int dealDataTo(String[] dataPaths, String[] newDataFilesPaths, String readDelimiter, String writeDelimiter) throws InvalidArrayOfPathsException, IOException {
		if(dataPaths.length <= 0 || newDataFilesPaths.length <= 0) {
			throw new InvalidArrayOfPathsException();
		}
		
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
	 * @throws InvalidArrayOfPathsException 
	 * @throws IOException 
	 * @returns int - The index of the last path written to, -1 if partitionData fails.
	 */
	public int dealStringsAsRecords(String[] newPaths, String readDelimiter, String writeDelimiter) throws InvalidArrayOfPathsException, IOException {
		if(newPaths.length <= 0) {
			throw new InvalidArrayOfPathsException();
		}
		
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
	 * @throws InvalidArrayOfPathsException 
	 * @throws IOException 
	 * @returns int - The index of the last record written to, -1 if partitionData fails.
	 */
	public static int dealStringsAsRecordsTo(String[] dataPaths, String[] newDataFilesPaths, String readDelimiter, String writeDelimiter) throws InvalidArrayOfPathsException, IOException {
		if(dataPaths.length <= 0 || newDataFilesPaths.length <= 0) {
			throw new InvalidArrayOfPathsException();
		}
		
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
	 * @throws InvalidArrayOfPathsException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws EOFException 
	 */
	public int dealRecords(String[] newPaths, String readDelimiter, String writeDelimiter) throws InvalidArrayOfPathsException, EOFException, ClassNotFoundException, IOException {
		if(newPaths.length <= 0) {
			throw new InvalidArrayOfPathsException();
		}
		
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
	 * @throws InvalidArrayOfPathsException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws EOFException 
	 * @returns int - The index of the last record written to, -1 if partitionData fails.
	 */
	public static int dealRecordsTo(String[] recordsPath, String[] newRecordFilesPaths, String readDelimiter, String writeDelimiter) throws InvalidArrayOfPathsException, EOFException, ClassNotFoundException, IOException {
		if(recordsPath.length <= 0 || newRecordFilesPaths.length <= 0) {
			throw new InvalidArrayOfPathsException();
		}
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
	 * @throws InvalidArrayOfPathsException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws EOFException 
	 */
	public void splitRecords(String[] destPaths, int numRecords, String readDelimiter, String writeDelimiter) throws InvalidArrayOfPathsException, EOFException, ClassNotFoundException, IOException {
		if(destPaths.length <= 0) {
			throw new InvalidArrayOfPathsException();
		}
		
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
	 * @param deleteSrcFiles
	 * @throws Exception 
	 */
	public static void mergeSortRecords(String[] srcPaths, String[] destPaths, String workingDir,
			String readDelimiter, String writeDelimiter, boolean deleteSrcFiles) throws Exception {
		if(!Util.isValidDirectory(workingDir)) {
			throw new DirectoryNotFoundException();
		}
		
		if(srcPaths.length <= 0 || destPaths.length <= 0) {
			throw new InvalidArrayOfPathsException();
		}
		
		LinkedList<RecordsFileIO> recsQueue = new LinkedList<RecordsFileIO>();
		for(int p = 0; p < srcPaths.length; p++) {
			RecordsFileIO recs = new RecordsFileIO(srcPaths[p], true, true);
			recs.sortRecords(workingDir, readDelimiter);
			recsQueue.add(recs);
		}
		
		int numPathsMerged = srcPaths.length;
		while(recsQueue.size() >= 2) {
			String destPath = Util.generateRandomPath(workingDir, "/mergeSortIntermediary_", "txt");
			RecordsFileIO recs1 =  recsQueue.remove();
			recs1.setIsReadFile(true);
			RecordsFileIO recs2 = recsQueue.remove();
			recs2.setIsReadFile(true);
			
			RecordsFileIO mergedRecs = new RecordsFileIO(destPath, true, false);
			mergeRecordsTo(recs1, recs2, mergedRecs, readDelimiter, readDelimiter);
			
			numPathsMerged--;
			if(deleteSrcFiles || numPathsMerged < 0) {
				recs1.delete();
			} else {
				recs1.close();
			}
			
			numPathsMerged--;
			if(deleteSrcFiles || numPathsMerged < 0) {
				recs2.delete();
			} else {
				recs2.close();
			}
			
			recsQueue.add(mergedRecs);
		}
		

		if(recsQueue.size() > 0) {
			RecordsFileIO mergedRecs = recsQueue.remove();
			mergedRecs.setIsReadFile(true);
			
			mergedRecs.splitRecords(destPaths, mergedRecs.getNumRecordsWritten(), readDelimiter, writeDelimiter);
			mergedRecs.delete();
		} else {
			// TODO: remove debugging
			System.out.println("RecordsFileIO.mergeSortRecords: Last element in recsQueue not found.");
			throw new RecordsFileIOException();
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
	 * @throws Exception 
	 */
	public static void mergeRecordsTo(RecordsFileIO recs1, RecordsFileIO recs2, RecordsFileIO mergedRecs, String readDelimiter, String writeDelimiter) throws Exception {
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
	 * Merge sorts records.
	 * @param workingDir
	 * @param readDelimiter
	 * @throws Exception 
	 */
	public void sortRecords(String workingDir, String readDelimiter) throws Exception {
		if(!Util.isValidDirectory(workingDir)) {
			throw new DirectoryNotFoundException();
		}
		
		if(isReadFile) {
			LinkedList<RecordsFileIO> mergeFiles = new LinkedList<RecordsFileIO>();
			Record recA;
			while((recA = readNextRecord(readDelimiter)) != null) {
				String destPath = Util.generateRandomPath(workingDir, "/sortRecordsIntermediary_", "txt");
				RecordsFileIO mergeFile = new RecordsFileIO(destPath, true, false);
				
				Record recB = readNextRecord(readDelimiter);
				
				if(recB != null) {
					int diff = recA.compare(recB);
					
					if(diff == 0) {
						recA.addValues(recB.getValues());
						mergeFile.writeNextRecord(recA, readDelimiter);
					} else if(diff < 0) {
						// write recA first
						mergeFile.writeNextRecord(recA, readDelimiter);
						mergeFile.writeNextRecord(recB, readDelimiter);
					} else {
						// write recB first
						mergeFile.writeNextRecord(recB, readDelimiter);
						mergeFile.writeNextRecord(recA, readDelimiter);
					}
				} else {
					mergeFile.writeNextRecord(recA, readDelimiter);
				}
								
				mergeFiles.add(mergeFile);
			
			}
			
			if(mergeFiles.size() > 0) {
				// Only do this if the file actually has records
				System.out.println("Initially there were: " + mergeFiles.size());
				
				while(mergeFiles.size() > 2) {
					RecordsFileIO recs1 =  mergeFiles.remove();
					recs1.setIsReadFile(true);
					RecordsFileIO recs2 = mergeFiles.remove();
					recs2.setIsReadFile(true);
					
					String destPath = Util.generateRandomPath(workingDir, "/sortRecordsIntermediary_", "txt");
					RecordsFileIO mergedRec = new RecordsFileIO(destPath, true, false);
					mergeRecordsTo(recs1, recs2, mergedRec, readDelimiter, readDelimiter);
					mergeFiles.add(mergedRec);
					recs1.delete();
					recs2.delete();
					
	
					System.out.println("There are now: " + mergeFiles.size());
				}
				
				
				if(mergeFiles.size() >= 2) {
					RecordsFileIO recs1 =  mergeFiles.remove();
					recs1.setIsReadFile(true);
					RecordsFileIO recs2 = mergeFiles.remove();
					recs2.setIsReadFile(true);
					
					delete();
					initialize(true, false);
					
					mergeRecordsTo(recs1, recs2, this, readDelimiter, readDelimiter);
					recs1.delete();
					recs2.delete();
					setIsReadFile(true);
					
				} else {
					System.out.println("RecordsFileIO.sortRecords: Last 2 elements in mergeFiles not found.");
					throw new RecordsFileIOException();
				}
			}
		}
	}
	
	/**
	 * Closes and deletes the file.
	 * @throws IOException 
	 */
	public void delete() throws IOException {
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
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Returns true if the file exists.
	 */
	public boolean exists() {
		return file != null && file.exists();
	}
}
