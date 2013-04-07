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
	private RecordsFileIO io;
	
	/**
	 * Collector - constructor
	 * @param path - path of file to write key-value pairs to (will create if not exist).
	 */
	public Collector(String path){
		this.pairs = new ArrayList<Record>();
		this.io = new RecordsFileIO(path, true, false);
	}
	
	/**
	 * collect - add a key-value pair to the collector.
	 */
	public void collect(Writable key, Writable value){
		Record pair = new Record(key, new Writable[]{value});
		pairs.add(pair);
	}
	
	public String getPath(){
		return io.getPath();
	}
	
	/**
	 * dumpBuffer - write all key-value pairs to file and clean the 'pairs' buffer.
	 */
	private void dumpBuffer(){
		Iterator<Record> itr = pairs.iterator();
		while(itr.hasNext()){
			Record record = itr.next();
			io.writeNextRecord(record, "\n"); //write buffer
		}
		pairs = new ArrayList<Record>(); //clean buffer
	}
}
