/**
 * Record is a key values pair to store intermediary results.
 */

package fileIO;

import java.io.Serializable;

import vansitest.tFile;

public class Record implements Serializable{
	private static final long serialVersionUID = -8308858610647756046L;
	public Object key;
	public Object[] values;
	
	// TODO: Change from Object to Writeable type
	public Record(Object key, Object[] values) {
		this.key = key;
		this.values = values;
	}
	
	public static void main(String[] args) {
		tFile t = new tFile("poop.txt", true);
		t.writeObj(new Record("key", new Object[]{"value"}));
		String data = t.read();
		System.out.println(data.indexOf("\n"));
	}
}
