package api;

public class ByteArrayWritable implements Writable {
	private static final long serialVersionUID = -5253960144227324995L;
	private Byte[] value;
	
	public ByteArrayWritable(Byte[] value){
		this.value = value;
	}
	
	public Byte[] getValue(){
		return value;
	}
}
