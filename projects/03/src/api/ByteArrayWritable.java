package api;

public class ByteArrayWritable implements Writable<Byte[]> {
	private static final long serialVersionUID = -5253960144227324995L;
	private Byte[] value;
	
	public ByteArrayWritable(Byte[] value){
		this.value = value;
	}
	
	@Override
	public Byte[] getValue(){
		return value;
	}

	@Override
	public int compare(Writable<Byte[]> w) {
		Byte[] myValArr = value;
		Byte[] wValArr  = w.getValue();
		
		//based on length first
		if(myValArr.length < wValArr.length){
			return -1;
		} else if(myValArr.length > wValArr.length){
			return 1;
		} else {
			//based on values second
			for(int i = 0; i < myValArr.length; i++){
				Byte myVal = myValArr[i];
				Byte wVal  = wValArr[i];
				
				int compare = myVal.compareTo(wVal);
				if(compare != 0){
					return compare;
				}
			}
			
			//the arrays are exactly the same
			return 0;
		}
	}
	
}
