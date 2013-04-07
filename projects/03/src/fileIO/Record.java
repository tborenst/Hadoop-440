/**
 * Record is a key values pair to store intermediary results.
 */

package fileIO;

import java.io.Serializable;

import api.StringWritable;
import api.Writable;

import vansitest.tFile;

public class Record implements Serializable{
	private static final long serialVersionUID = -8308858610647756046L;
	public Writable key;
	public Writable[] values;
	
	// TODO: Change from Object to Writeable type
	public Record(Writable key, Writable[] values) {
		this.key = key;
		this.values = values;
	}
	
	// TODO: remove main function
	public static void main(String[] args) {
		tFile t = new tFile("poop.txt", true);
		Writable[] writablearray = {new StringWritable("value")};
		t.writeObj(new Record(new StringWritable("hey"), writablearray));
		String data = t.read();
		System.out.println(data.indexOf("\n"));
	}
}
