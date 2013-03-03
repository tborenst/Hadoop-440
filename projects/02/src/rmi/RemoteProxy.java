package rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class RemoteProxy extends Proxy{
	RemoteObjectReference ror; //ip, port, objectUID
	private static final long serialVersionUID = 1L;

	public RemoteProxy(InvocationHandler handler, String hostname, int port, String objectUID) {
		super(handler);
		this.ror = new RemoteObjectReference(hostname, port, objectUID);
	}


	public static RemoteProxy newRemoteProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h, String hostname, int port, String objectUID) {
		RemoteProxy rp = new RemoteProxy(h, hostname, port, objectUID);
		rp.ror = new RemoteObjectReference(hostname, port, objectUID);
		return rp;
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
