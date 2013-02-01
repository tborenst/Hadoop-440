/**
 * This class allows SocketIO to call Runnable objects with the ability to pass in parameters.
 * SIOCommand takes a String array, args, as a parameter. This array can later be referred to when
 * declaring a new SIOCommand with a custom run() function.
 */
package networking;

public class SIOCommand implements Runnable{
	protected String[] args;
	
	public SIOCommand(){
		this.args = null;
	}
	
	public void parameters(String[] args){
		this.args = args;
	}
	
	@Override
	public void run() {
		//do nothing by default
	}

}
