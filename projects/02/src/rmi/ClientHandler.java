/**
 * ClientHandler is the interface through which the client can manage its interactions 
 * with RMI servers: connect to new servers or lookup an existing remote object.
 * Author: Vansi Vallabhaneni
 */

package rmi;

import java.lang.reflect.Proxy;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;


import rmimessage.RMINamingRequest;
import rmimessage.RMINamingResponse;

import vansitest.Person;

import networking.SIOClient;

public class ClientHandler {
	private HashMap<String, Class<?>> implInterfaces;
	private HashMap<String, SIOClient> connections;
	private HashMap<String, RemoteObjectReference> nameToROR;

	/**
	 * Constructor for ClientHandler.
	 */
	public ClientHandler() {
		this.implInterfaces = new HashMap<String, Class<?>>();
		this.connections = new HashMap<String, SIOClient>();
		this.nameToROR = new HashMap<String, RemoteObjectReference>();
	}
	
	/**
	 * Add a supported Interface so the user can create Proxies of objects which implement this interface (newInterface)
	 * @param interfaceClass - the interface to register on the client
	 */
	public void registerInterface(Class<?> interfaceClass) {
		implInterfaces.put(interfaceClass.getSimpleName(), interfaceClass);
	}
	
	/**
	 * Create a new socket connection to a server (hostname:port).
	 * @param hostname
	 * @param port
	 */
	public SIOClient connectTo(String hostname, int port) {
		SIOClient newConnection = new SIOClient(hostname, port);
		connections.put(hostname + ":" + port, newConnection);
		return newConnection;
	}
	
	/**
	 * Binds the specified name to a remote object.
	 * @param name
	 * @param ror
	 * @throws Exception
	 */
	public void bind(String name, RemoteObjectReference ror) throws Exception {
		if(ror == null) {throw new NoSuchRemoteObjectReferenceException();}
		SIOClient socket = connections.get(ror.hostname + ":" + ror.port);
		if(socket != null && socket.isAlive()) {
			RMINamingRequest objRequestData = new RMINamingRequest(name, ror);
			RMINamingResponse objResponseData = (RMINamingResponse) socket.request("bindObject", objRequestData);
			
			if(objResponseData.isError) {
				throw (Exception) objResponseData.response;
			} else {
				nameToROR.put(name, ror);
			}
		}
		else {
			throw new RemoteException();
		}
	}
	
	/**
	 * Rebinds the specified name to a new remote object. Any existing binding for the name is replaced.
	 * @param name
	 * @param ror
	 * @throws Exception
	 */
	public void rebind(String name, RemoteObjectReference ror) throws Exception {
		if(ror == null) {throw new NoSuchRemoteObjectReferenceException();}
		SIOClient socket = connections.get(ror.hostname + ":" + ror.port);
		if(socket.isAlive()) {
			try {
				unbind(name);
			} catch (NotBoundException e) {}
			
			RMINamingRequest objRequestData = new RMINamingRequest(name, ror);
			RMINamingResponse objResponseData = (RMINamingResponse) socket.request("rebindObject", objRequestData);
			
			if(objResponseData.isError) {
				throw (Exception) objResponseData.response;
			} else {
				nameToROR.put(name, ror);
			}
		}
		else {
			throw new RemoteException();
		}
	}
	
	/**
	 * Destroys the binding for the specified name that is associated with a remote object.
	 * @param name
	 * @throws Exception
	 */
	public void unbind(String name) throws Exception {
		RemoteObjectReference ror = nameToROR.get(name);
		if(ror != null) {
			SIOClient socket = connections.get(ror.hostname + ":" + ror.port);
			if(socket.isAlive()) {
				RMINamingRequest objRequestData = new RMINamingRequest(name, null);
				RMINamingResponse objResponseData = (RMINamingResponse) socket.request("unbindObject", objRequestData);
				
				if(objResponseData.isError) {
					throw (Exception) objResponseData.response;
				} else {
					nameToROR.remove(name);
				}
			}
			else {
				throw new RemoteException();
			}
		}
	}
	
	/**
	 * Look for a remote object with name on the connected servers. 
	 * Throws NotBoundException if unable to find a remote object.
	 * Returns a Proxy of the remote object.
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public Proxy lookup(String name) throws Exception {
		for(SIOClient socket : connections.values()) {
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
	private Proxy lookupOn(SIOClient socket, String name) throws Exception {
		if(socket.isAlive()) {
			RMINamingRequest objRequestData = new RMINamingRequest(name, null);
			RMINamingResponse objResponseData = (RMINamingResponse) socket.request("lookupObject", objRequestData);
			
			if(objResponseData.isError) {
				throw (Exception) objResponseData.response;
			}
			
			RemoteObjectReference ror = (RemoteObjectReference) objResponseData.response;
			nameToROR.put(name, ror);
			return makeProxy(ror, socket);
		}
		else {
			throw new RemoteException();
		}
	}
	
	/**
	 * Constructs a new Proxy object for the client.
	 * @param ror
	 * @param socket
	 * @return
	 * @throws UnaddedInterfaceException
	 */
	public Proxy makeProxy(RemoteObjectReference ror, SIOClient socket) throws UnaddedInterfaceException {
		Class<?> myInterface = implInterfaces.get(ror.interfaceName);
		if(myInterface == null) {throw new UnaddedInterfaceException();}
		else {
			Stub handler = new Stub(ror, socket, this);
			
			Proxy foundObj = (Proxy) Proxy.newProxyInstance(myInterface.getClassLoader(),
											new Class[] { myInterface }, handler);
			return foundObj;
		}
	}

}
