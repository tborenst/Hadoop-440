package rmi;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import vansitest.Person;

import networking.SIOClient;

public class ClientManager {
	//private HashMap<Proxy, RemoteObjectReference> cache;
	//private HashMap<String, SIOClient> connections;
	private HashMap<String, Class<?>> implInterfaces;
	private ArrayList<SIOClient> connections;

	public ClientManager() {
		//this.cache = new HashMap<Proxy, RemoteObjectReference>();
		//this.connections = new HashMap<String, SIOClient>();
		this.implInterfaces = new HashMap<String, Class<?>>();
		this.connections = new ArrayList<SIOClient>();
	}
	
	//for testing
	public void addInterface(String interfaceName, Class<?> newInterface) {
		implInterfaces.put(interfaceName, newInterface);
	}
	
	
	public void connectTo(String hostname, int port) {
		connections.add(new SIOClient(hostname, port));
	}
	
	//TODO: handle removing connections, when a socket closes
	
	public Proxy lookup(String name) throws NotBoundException, AccessException, RemoteException, MalformedURLException {
		for(int c = 0; c < connections.size(); c++) {
			SIOClient socket = connections.get(c);
			
			try {
				return lookupOn(socket, name);
			} catch (NotBoundException e) {}
		}
		throw new NotBoundException();
	}
	
	private Proxy lookupOn(SIOClient socket, String name) throws NotBoundException, RemoteException, AccessException, MalformedURLException {
		RMIObjRequest objRequestData = new RMIObjRequest(name);
		RMIObjResponse objResponseData = (RMIObjResponse) socket.request("lookup", objRequestData);
		
		if(objResponseData.isThrowable) {
			// TODO Figure out something better than the try and catch
			try {
				throw (Exception) objResponseData.response;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		RemoteObjectReference ror = (RemoteObjectReference) objResponseData.response;
		
		Class<?> myInterface = implInterfaces.get(ror.interfaceName);
		
		//TODO: check if myInterface is null, and throw an unimplementedInterfaceException (looks like we need to create this exception)
		//if(myInterface == null) {throw new UnimplementedInterfaceException();}
		
		Stub handler = new Stub(ror, socket);
		
		Proxy foundObj = (Proxy) Proxy.newProxyInstance(myInterface.getClassLoader(),
				new Class[] { myInterface }, handler);
		return foundObj;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClientManager client = new ClientManager();
		client.addInterface(Person.class.getSimpleName(), Person.class);
	}

}
