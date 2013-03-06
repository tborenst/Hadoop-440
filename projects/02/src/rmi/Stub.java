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

	/**
	 * Constructor for Stub
	 * @param ror
	 * @param socket
	 * @param client
	 */
	public Stub(RemoteObjectReference ror, SIOClient socket, ClientHandler client) {
		System.out.println("New STUB with ROR: "+ror);
		this.ror = ror;
		this.socket = socket;
		this.client = client;
	}
	
	/**
	 * Invoke handles function calls on the client side remote objects.
	 * @param proxy
	 * @param method
	 * @param args
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
		if(method.getName().equals("getROR") && args == null) {
			return ror;
		}
		
		if(socket.isAlive()) {
			int argsLength = 0;
			if(args != null) {argsLength = args.length;}
			
			boolean[] remotes = new boolean[argsLength];
			for(int i = 0; i < argsLength; i++) {
				if(args[i] instanceof Proxy) {
					args[i] = ((MyRemote) args[i]).getROR();
					remotes[i] = true;
				}
				else {
					remotes[i] = false;
				}
			}
					
			RMIRequest requestData = new RMIRequest(ror, method, args, remotes);
			RMIResponse responseData = (RMIResponse) socket.request("invokeMethod", requestData);
		
			Object result = responseData.response;
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
}
