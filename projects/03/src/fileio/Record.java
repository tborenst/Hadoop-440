/**
 * Record is a key values pair to store intermediary results.
 */

package fileio;

import java.io.Serializable;

import api.IntWritable;
import api.StringWritable;
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
	 * this - rec
	 * @param rec
	 * @return int
	 */
	public int compare(Record rec) {
		if(rec == null) {
			return -1;
		}
		return key.compare(rec.getKey());
	}
	
	/**
	 * Appends newValues to values array.
	 * @param newValues
	 */
	public void addValues(Writable[] newValues) {
		Writable[] mergedValues = new Writable[values.length + newValues.length];
		
		for(int v = 0; v < values.length; v++) {
			mergedValues[v] = values[v];
		}
		
		for(int v = 0; v < newValues.length; v++) {
			mergedValues[v + values.length] = newValues[v];
		}
		
		values = mergedValues;
	}
	
	public String toString() {
		String result = "<" + key.getValue() + ", [";
		for(int v = 0; v < values.length; v++) {
			if(v != 0) {
				result += ", ";
			}
			result += values[v].getValue();
		}
		result += "]>";
		return result;
	}
	
	public boolean equals(Record r) {
		return key.equals(r.getKey());
	}
}
