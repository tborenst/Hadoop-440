package examples;

import rmi.MyRemote;

public interface RemoteCounterFactory extends MyRemote{
	public RemoteCounter makeRemoteCounter(int increment);
}
