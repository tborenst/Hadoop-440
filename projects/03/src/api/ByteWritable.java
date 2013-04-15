package api;

public class ByteWritable implements Writable<Byte> {
	private static final long serialVersionUID = 5864951662997680711L;
	private Byte value;
	
	public ByteWritable(Byte value){
		this.value = value;
	}
	
	@Override
	public Byte getValue(){
		return value;
	}

	@Override
	public int compare(Writable<Byte> w) {
		Byte myVal = value;
		Byte wVal  = w.getValue();
		return myVal.compareTo(wVal);
	}
}
