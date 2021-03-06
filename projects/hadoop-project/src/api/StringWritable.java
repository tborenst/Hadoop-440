/**
 * Writable wrapper around a String object.
 */
package api;

public class StringWritable implements Writable<String>{
	private static final long serialVersionUID = -8005386730763170158L;
	
	private String value;
	
	public StringWritable(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}

	public int compare(Writable<String> w) {
		String myVal = value;
		String wVal  = w.getValue();
		return myVal.compareTo(wVal);
	}

}
