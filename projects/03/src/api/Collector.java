/**
 * The Collector class is used by map and reduce functions to store key-value pairs.
 * The Collector buffers key-value pairs in memory for a while and then writes them to disk to prevent memory overflow.
 * @author Tomer Borenstein
 */
package api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import fileIO.Record;
import fileIO.RecordsFileIO;

public class Collector implements Serializable{
	private static final long serialVersionUID = -3698360081977942111L;
	
	private ArrayList<Record> pairs;
	private String path;
	private RecordsFileIO io;
	private String delimiter = "\n";
	
	/**
	 * Collector - constructor
	 * @param path - path of file to write key-value pairs to (will create if not exist).
	 */
	public Collector(String path){
		this.pairs = new ArrayList<Record>();
		this.path = path;
		this.io = new RecordsFileIO(path, true, false);
	}
	
	/**
	 * collect - add a key-value pair to the collector.
	 */
	public void collect(Writable key, Writable value){
		Record pair = new Record(key, new Writable[]{value});
		pairs.add(pair);
	}
	
	/**
	 * collectAllFromFile - erases the current collection buffer and reads all records
	 * from the file given by the path of this Collector.
	 */
	public void collectAllFromFile(){
		io.setIsReadFile(true); //set mode to "reading"
		pairs = new ArrayList<Record>(); //erase current buffer
		
		//load all from file into buffer
		Record rec;
		while((rec = io.readNextRecord(delimiter)) != null){
			pairs.add(rec);
		}
		
		io.setIsReadFile(false); //set mode back to "writing"
	}
	
	public String getPath(){
		return path;
	}
	
	/**
	 * dumpBuffer - write all key-value pairs to file and clean the 'pairs' buffer.
	 */
	public void dumpBuffer(){
		Iterator<Record> itr = pairs.iterator();
		while(itr.hasNext()){
			Record record = itr.next();
			io.writeNextRecord(record, delimiter); //write buffer
		}
		pairs = new ArrayList<Record>(); //clean buffer
	}
	
	/**
	 * DEBUGGING FUNCTION - PRINT ALL RECORDS
	 */
	public void printAllRecords(){
		Iterator<Record> it = pairs.iterator();
		System.out.println("======================");
		System.out.println("PRINTING ALL RECORDS: ");
		while(it.hasNext()){
			Record rec = it.next();
			System.out.println("KEY: " + rec.getKey().getValue());
			for(int i = 0; i < rec.getValues().length; i++){
				System.out.println("V " + i + ": " + rec.getValues()[i].getValue());
			}
		}
		System.out.println("======================");
	}
}
