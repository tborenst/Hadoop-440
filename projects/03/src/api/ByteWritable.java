package api;

public class ByteWritable implements Writable {
	private static final long serialVersionUID = 5864951662997680711L;
	private Byte value;
	
	public ByteWritable(Byte value){
		this.value = value;
	}
	
	public Byte getValue(){
		return value;
	}
}
