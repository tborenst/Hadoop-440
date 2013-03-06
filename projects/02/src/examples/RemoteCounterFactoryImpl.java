package examples;

import rmi.RemoteObjectReference;

public class RemoteCounterFactoryImpl implements RemoteCounterFactory{
	private static final long serialVersionUID = 4345420744519447501L;

	public RemoteObjectReference getROR() {
		return null;
	}

	public RemoteCounter makeRemoteCounter(int increment) {
		return new RemoteCounterImpl(increment);
	}
	
}
