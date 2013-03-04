/**
 * Stub marshals requests between the Proxy and the 
 * RMI server which contains the correlating remote object.
 * Author: Vansi Vallabhaneni
 */

package rmi;

import java.lang.reflect.*;
import java.rmi.RemoteException;

import rmimessage.RMIRequest;
import rmimessage.RMIResponse;

import networking.SIOClient;


public class Stub implements InvocationHandler {
	private RemoteObjectReference ror;
	private SIOClient socket;
	private ClientHandler client;


	
	public Stub(RemoteObjectReference ror, SIOClient socket, ClientHandler client) {
		this.ror = ror;
		this.socket = socket;
		this.client = client;
	}
	
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
		System.out.println("Method: " + method.toString());
		
		if(args != null) {
			System.out.println("Args: " + args.toString());
		}
		
		
		RMIRequest requestData = new RMIRequest(ror, method, args);
		//System.out.println("Stub: Sending Ror " + requestData.methodName);
		RMIResponse responseData = (RMIResponse) socket.request("invokeMethod", requestData);
		
		if(socket.isAlive()) {
			Object result = responseData.response;
			
			//check response for errors (isThrowable)
			if(responseData.isError) {
				throw (Exception) responseData.response;
			} else if(responseData.isROR) {
				RemoteObjectReference resultROR = (RemoteObjectReference) responseData.response;
				result = client.makeProxy(resultROR, socket);
			}
			
			return result;
		}
		else {
			throw new RemoteException();
		}
	}

	
	
	
	public static void main(String[] args) {
		
		/*InvocationHandler stub = new ClientHandler(); //not actually stub right???, should only be one instance per client
		Proxy personProxy = (Proxy) Proxy.newProxyInstance(Person.class.getClassLoader(),
										new Class[] { Person.class }, stub);
		Person p = (Person) personProxy;
		//p.aldsflkjafds();
		System.out.println(p.getAge()); */

	}



}
