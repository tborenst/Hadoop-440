package rmi;

import java.lang.reflect.*;
import java.util.HashMap;

import vansitest.Person;

import networking.SIOClient;


public class ClientHandler implements InvocationHandler {

	private HashMap<String, SIOClient> serverSockets; //maps hostname:port to client socket




	public ClientHandler() {
		this.serverSockets = new HashMap<String, SIOClient>();
	}
	
	public SIOClient addConnection(String hostname, int port) {
		SIOClient socket = new SIOClient(hostname, port);
		serverSockets.put(hostname+":"+port, socket);
		return socket;
	}
	
	public SIOClient getConnection(String hostname, int port) {
		return serverSockets.get(hostname+":"+port);
	}
	
	public SIOClient getConnection(RemoteObjectReference ror) {
		return getConnection(ror.hostname, ror.port);
	}
	
	public RemoteProxy getObject(String hostname, int port, String objectUID) throws Exception {
		RMIObjRequest request = new RMIObjRequest(objectUID);
		RMIResponse response = (RMIResponse) getConnection(hostname, port).request("objExists", request);
		
		if(response.isThrowable) {
			throw (Exception) response.response;
		}
		
		
		return (RemoteProxy) response.response;
	}
	

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
		RemoteProxy remProx = (RemoteProxy) proxy;
		//System.out.println("Obj: "+obj.toString());
		System.out.println("Method: " + method.toString());
		
		if(args != null) {
			System.out.println("Args: " + args.toString());
		}
		
		SIOClient sock = getConnection(remProx.ror);
		RMIRequest message = new RMIRequest(remProx.ror.objectUID, method, args);
		RMIResponse response = (RMIResponse) sock.request("invokeMethod", message);
		
		System.out.println(response.toString());
	
		if(response.isThrowable) {
			throw (Exception) response.response;
		}
		
		return response.response;
		//throw errors and whatever as necessary
	}

	
	
	
	public static void main(String[] args) {
		
		InvocationHandler stub = new ClientHandler(); //not actually stub right???, should only be one instance per client
		Person p = (Person) RemoteProxy.newRemoteProxyInstance(Person.class.getClassLoader(),
										new Class[] {Person.class}, stub, "hostname", 8080, "0");
		//p.aldsflkjafds();
		System.out.println(p.getAge()); 

	}



}
