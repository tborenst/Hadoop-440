package networking;

public class SIOCommand implements Runnable{
	private Object object;
	
	public SIOCommand(){
		this.object = null;
	}
	
	public void passObject(Object object){
		this.object = object;
	}
	
	@Override
	public void run() {
		//do nothing by default
	}
	
}
