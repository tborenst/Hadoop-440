package examples;

import rmi.RemoteObjectReference;

public class RemoteCounterImpl implements RemoteCounter{
	private static final long serialVersionUID = -3792280235539839825L;
	
	private int count;
	private int increment;
	
	public RemoteCounterImpl(int increment){
		this.count = 0;
		this.increment = increment;
	}
	
	public RemoteObjectReference getROR() {
		return null;
	}
	
	public int getCount() {
		return count;
	}

	public void stepCount() {
		count += increment;
	}
	
}
