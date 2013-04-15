/**
 * The Collector class is used by map and reduce functions to store key-value pairs.
 * Collector buffers key-value pairs in memory for a while and then uses dumpBuffer() to write them to disk to prevent memory overflow.
 * @author Tomer Borenstein
 */
package api;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import fileio.Record;
import fileio.RecordsFileIO;

public class Collector implements Serializable{
	private static final long serialVersionUID = -3698360081977942111L;
	
	private ArrayList<Record> pairs;
	private int pairCount;
	private String path;
	private RecordsFileIO io;
	private String delimiter = "\n";
	
	/**
	 * Collector - constructor
	 * @param path - path of file to write key-value pairs to (will create if not exist).
	 * @param read - should the collector be initially set to read? If false, it will override the file given by its path.
	 * @throws IOException 
	 */
	public Collector(String path) throws IOException{
		this.pairs = new ArrayList<Record>();
		this.pairCount = 0;
		this.path = path;
		this.io = new RecordsFileIO(path, true, false);
	}
	
	/**
	 * emit - add a key-value pair to the collector.
	 */
	public void emit(Writable key, Writable value){
		Record pair = new Record(key, new Writable[]{value});
		pairs.add(pair);
		if(50 < pairCount){
			dumpBuffer();
			pairCount = 0;
		}
	}
	
	/**
	 * emitString - write a string to file, instead of a record, so output will be human readable.
	 * Note: strings will be separated by newlines.
	 */
	public void emitString(String str){
		io.writeNextString(str, "\n");
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
	
	public void close(){
		io.close();
	}
	
	/**
	 * DEBUGGING FUNCTION - PRINT ALL RECORDS (to stdout)
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
