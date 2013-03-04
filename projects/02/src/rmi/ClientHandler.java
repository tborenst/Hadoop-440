/**
 * ClientHandler is the interface through which the client can manage its interactions 
 * with RMI servers: connect to new servers or lookup an existing remote object.
 * Author: Vansi Vallabhaneni
 */

package rmi;

import java.lang.reflect.Proxy;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import rmimessage.RMINamingRequest;
import rmimessage.RMINamingResponse;

import vansitest.Person;

import networking.SIOClient;

public class ClientHandler {
	private HashMap<String, Class<?>> implInterfaces;
	private ArrayList<SIOClient> connections;

	/**
	 * Constructor for ClientHandler.
	 */
	public ClientHandler() {
		this.implInterfaces = new HashMap<String, Class<?>>();
		this.connections = new ArrayList<SIOClient>();
	}
	
	/**
	 * Add a supported Interface so the user can create Proxies of objects which implement this interface (newInterface)
	 * @param interfaceName
	 * @param newInterface
	 */
	public void addInterface(String interfaceName, Class<?> newInterface) {
		implInterfaces.put(interfaceName, newInterface);
	}
	
	/**
	 * Create a new socket connection to a server (hostname:port).
	 * @param hostname
	 * @param port
	 */
	public SIOClient connectTo(String hostname, int port) {
		SIOClient newConnection = new SIOClient(hostname, port);
		connections.add(newConnection);
		return newConnection;
	}
	
	//TODO: handle removing connections, when a socket closes
	
	/**
	 * Look for a remote object with name on the connected servers. 
	 * Throws NotBoundException if unable to find a remote object.
	 * Returns a Proxy of the remote object.
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public Proxy lookup(String name) throws Exception {
		for(int c = 0; c < connections.size(); c++) {
			SIOClient socket = connections.get(c);
			
			try {
				return lookupOn(socket, name);
			} catch (NotBoundException e ) {}
			catch(RemoteException e) {}
		}
		throw new NotBoundException();
	}
	
	/**
	 * Look for a remote object with name on the server connected to with socket. 
	 * Throws NotBoundException if unable to find a remote object.
	 * Returns a Proxy of the remote object.
	 * @param socket
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public Proxy lookupOn(SIOClient socket, String name) throws Exception {
		RMINamingRequest objRequestData = new RMINamingRequest(name);
		if(socket.isAlive()) {
			RMINamingResponse objResponseData = (RMINamingResponse) socket.request("lookupObject", objRequestData);
			
			if(objResponseData.isError) {
				// TODO Figure out something better than the try and catch
				throw (Exception) objResponseData.response;
			}
			
			RemoteObjectReference ror = (RemoteObjectReference) objResponseData.response;
			return makeProxy(ror, socket);
		}
		else {
			throw new RemoteException();
		}
	}
	
	public Proxy makeProxy(RemoteObjectReference ror, SIOClient socket) {
		Class<?> myInterface = implInterfaces.get(ror.interfaceName);
		
		//TODO: check if myInterface is null, and throw an unimplementedInterfaceException (looks like we need to create this exception)
		//if(myInterface == null) {throw new UnimplementedInterfaceException();}
		
		Stub handler = new Stub(ror, socket, this);
		
		Proxy foundObj = (Proxy) Proxy.newProxyInstance(myInterface.getClassLoader(),
				new Class[] { myInterface }, handler);
		return foundObj;
	}
	
	//testing function
	public static void main(String[] args) throws Exception {
		ClientHandler client = new ClientHandler();
		client.addInterface(Person.class.getSimpleName(), Person.class);
		Person p = (Person) client.lookupOn(null, null);
		p.getName();
	}

}
