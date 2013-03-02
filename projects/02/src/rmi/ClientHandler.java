package rmi;

import java.lang.reflect.*;
import java.util.HashMap;

import vansitest.Person;

public class ClientHandler implements InvocationHandler {

	private HashMap<String, SIOClient> serverSocks; //maps hostname:port to client socket




	public ClientHandler() {
		this.serverSocks = new HashMap<String, SIOClient>();
	}
	
	public SIOClient addConnection(String hostname, int port) {
		serverSocks.put(hostname+":"+port, SIOClient(hostname, port));
	}
	
	public SIOClient getConnection(String hostname, int port) {
		serverSocks.get(hostname+":"+port);
	}
	
	public SIOClient getConnection(RemoteProxy ror) {
		return getConnection(ror.hostname, ror.port);
	}
	

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		//System.out.println("Obj: "+obj.toString());
		System.out.println("Method: "+method.toString());
		if(args != null) 
		{System.out.println("Args: "+args.toString());}
		
		SIOClient sock = getConnection(proxy.ror);
		RMIRequest message = new RMIRequest(proxy.ror.objectUID, method, args);
		RMIResponse response = (RMIResponse) sock.request("request", message);
		
		System.out.println(response.toString());
	
		if(response.isThrowable) {
			throw (Exception) response.response;
		}
		
		return response.response;
		//throw errors and whatever as necessary
	}

	
	
	
	public static void main(String[] args) {
		
		InvocationHandler stub = new ClientHandler(); //not actually stub right???, should only be one instance per client
		Person p = (Person) RemoteProxy.newProxyInstance(Person.class.getClassLoader(),
										new Class[] {Person.class}, stub);
		//p.aldsflkjafds();
		System.out.println(p.getAge());

	}



}
