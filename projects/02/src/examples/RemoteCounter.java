package examples;

import rmi.MyRemote;

public interface RemoteCounter extends MyRemote{
	public int getCount();
	public void stepCount();
}
