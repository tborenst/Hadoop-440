/**
 * Writable wrapper around an Integer object.
 */
package api;

public class IntWritable implements Writable{
	private static final long serialVersionUID = 8741473372756572666L;
	private Integer value;
	
	public IntWritable(Integer value){
		this.value = value;
	}
	
	public Integer getValue(){
		return value;
	}
}