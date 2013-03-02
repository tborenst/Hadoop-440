package rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class RemoteProxy extends Proxy{
	RemoteObjectReference ror; //ip, objectUID
	private static final long serialVersionUID = 1L;

	protected RemoteProxy(InvocationHandler handler, String hostname, int port, String objectUID) {
		super(handler);
		this.ror = new RemoteObjectReference(hostname, port, objectUID);
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
