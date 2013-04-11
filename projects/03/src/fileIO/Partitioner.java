/**
 * The Partitioner class is a wrapper around RecordsFileIO that serves to act as a clean way for machines to read in data.
 * Data can be plain text or a file of serialized Record objects created by another machine.
 */
package fileio;

import java.util.HashMap;

public class Partitioner {
	
	HashMap<String, RecordsFileIO> readers;
	
	public Partitioner(){
		this.readers = new HashMap<String, RecordsFileIO>();
	}
	
	/**
	 * partitionPlainText - partitions plain text into #partition files such that each record is
	 * given as <IntWritable (line number), StringWritable (line content)>, and all records are
	 * delimited by a newline.
	 */
	//TODO: make RecordsFileIO work with just knowing the number of partitions, not recPerPartition
	public void partitionPlainText(String oldPath, String[] newPaths, int recPerPartitions){
		Record line   = null;
		int recCount  = 1; //how many records we've written, 1 because of assignment in loop conditional below
		int pathCount = 0;
		String path   = newPaths[pathCount];
		
		RecordsFileIO readIO  = new RecordsFileIO(oldPath, false, true);
		RecordsFileIO writeIO = new RecordsFileIO(path, true, false);
		
		while((line = readIO.readNextString("\n")) != null){
			//adjust file to write to if needed
			if(recPerPartitions < recCount){
				pathCount++;
				recCount = 0;
				path = newPaths[pathCount];
				writeIO = new RecordsFileIO(path, true, false);
			}
			
			//write record to file
			writeIO.writeNextRecord(line, "\n");
			recCount++;
		}
	}
	
	public Record readNextRecord(String path, String delimiter){
		//get reader or create one if it doesn't exist
		RecordsFileIO reader = readers.get(path);
		if(reader == null){
			reader = new RecordsFileIO(path, false, true);
			readers.put(path, reader);
		}
		
		return reader.readNextRecord(delimiter);
	}
}
