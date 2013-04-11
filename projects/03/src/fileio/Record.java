/**
 * Record is a key values pair to store intermediary results.
 */

package fileIO;

import java.io.Serializable;
import api.Writable;


public class Record implements Serializable{
	private static final long serialVersionUID = -8308858610647756046L;
	private Writable key;
	private Writable[] values;
	
	public Record(Writable key, Writable[] values) {
		this.key = key;
		this.values = values;
	}
	
	public Writable getKey(){
		return key;
	}
	
	public Writable[] getValues(){
		return values;
	}
	
	public int compare(Record rec) {
		return key.compare(rec.getKey());
	}
}
