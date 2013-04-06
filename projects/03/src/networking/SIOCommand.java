package networking;

public class SIOCommand implements Runnable{
	protected Object object;
	protected SIOSocket socket;
	protected int requestId;
	
	public SIOCommand(){
		this.object = null;
		this.socket = null;
		this.requestId = -1;
	}
	
	public void passObject(Object object){
		this.object = object;
	}
	
	public void passSocket(SIOSocket socket){
		this.socket = socket;
	}
	
	public void passRequestId(int id){
		this.requestId = id;
	}
	
	@Override
	public void run() {
		//do nothing by default
	}
	
}
