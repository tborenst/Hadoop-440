/**
 * Writable wrapper around a String object.
 */
package api;

public class StringWritable implements Writable{
	private static final long serialVersionUID = -8005386730763170158L;
	
	private String value;
	
	public StringWritable(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}
}
