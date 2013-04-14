/**
 * Writable wrapper around an Integer object.
 */
package api;

public class IntWritable implements Writable<Integer>{
	private static final long serialVersionUID = 8741473372756572666L;
	private Integer value;
	
	public IntWritable(Integer value){
		this.value = value;
	}
	
	public Integer getValue(){
		return value;
	}

	public int compare(Writable<Integer> w) {
		Integer myVal = value;
		Integer wVal  = w.getValue();
		
		if(myVal < wVal){
			return -1;
		} else if(myVal == wVal){
			return 0;
		} else {
			return 1;
		}
	}
}