/**
 * Record is a key values pair to store intermediary results.
 */

package fileio;

import java.io.Serializable;
import api.Writable;


public class Record implements Serializable{
	private static final long serialVersionUID = -8308858610647756046L;
	private Writable key;
	private Writable[] values;
	
	/**
	 * Constructor for Record.
	 * @param key
	 * @param values
	 */
	public Record(Writable key, Writable[] values) {
		this.key = key;
		this.values = values;
	}
	
	/**
	 * Getter for key.
	 * @return Writable
	 */
	public Writable getKey(){
		return key;
	}
	
	/**
	 * Getter for values.
	 * @return Writable[]
	 */
	public Writable[] getValues(){
		return values;
	}
	
	/**
	 * Compares two records based off keys.
	 * @param rec
	 * @return int
	 */
	public int compare(Record rec) {
		return key.compare(rec.getKey());
	}
}
