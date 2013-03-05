/**
 * ServerHandler is the interface through which the requests are marshaled and 
 * methods invoked after which results are sent back to the requesters.
 * Author: Vansi Vallabhaneni
 */

package rmi;

import java.lang.reflect.*;
import java.rmi.AlreadyBoundException;
import java.util.HashMap;

import rmimessage.RMINamingRequest;
import rmimessage.RMINamingResponse;
import rmimessage.RMIRequest;
import rmimessage.RMIResponse;
import vansitest.Util;

import networking.SIOCommand;
import networking.SIOServer;


public class ServerHandler {
	public RMIIndex RMIIndex;
	private HashMap<Class<?>, Class<?>> primToObj;
	private SIOServer serverSocket;
	public ServerHandler(int port, Class<?> remoteInterface) {
		this.RMIIndex = new RMIIndex();
		this.serverSocket = new SIOServer(port);
		this.primToObj = new HashMap<Class<?>, Class<?>>();
		primToObj.put(boolean.class, Boolean.class);
		primToObj.put(char.class, Character.class);
		primToObj.put(byte.class, Byte.class);
		primToObj.put(short.class, Short.class);
		primToObj.put(int.class, Integer.class);
		primToObj.put(long.class, Long.class);
		primToObj.put(float.class, Float.class);
		primToObj.put(double.class, Double.class);
		primToObj.put(void.class, Void.class);

		serverSocket.on("invokeMethod", new SIOCommand() {
			public void run() {
				RMIRequest requestData = (RMIRequest) object;
				System.out.println("Server: ror: "+requestData.ror.objectUID);
				RMIResponse response = handle(requestData); //arg0 = RMIRequest
				socket.respond(requestId, response);
			}
		});
		
		serverSocket.on("lookupObject", new SIOCommand() {
			public void run() {
				System.out.println("Server: recieved a lookupObject request.");
				RMINamingResponse response = lookup((RMINamingRequest) object);
				System.out.println("Responding: error=" + response.isError);
				socket.respond(requestId, response);
			}
		});
		
		serverSocket.on("bindObject", new SIOCommand() {
			public void run() {
				System.out.println("Server: recieved a bindObject request.");
				RMINamingResponse response = bind((RMINamingRequest) object);
				System.out.println("Responding: error=" + response.isError);
				socket.respond(requestId, response);
			}
		});
		
		serverSocket.on("rebindObject", new SIOCommand() {
			public void run() {
				System.out.println("Server: recieved a bindObject request.");
				RMINamingResponse response = rebind((RMINamingRequest) object);
				System.out.println("Responding: error=" + response.isError);
				socket.respond(requestId, response);
			}
		});
		
		serverSocket.on("unbindObject", new SIOCommand() {
			public void run() {
				System.out.println("Server: recieved a bindObject request.");
				RMINamingResponse response = unbind((RMINamingRequest) object);
				System.out.println("Responding: error=" + response.isError);
				socket.respond(requestId, response);
			}
		});
	}
	
	public RMINamingResponse bind(RMINamingRequest request) {
		Object result;
		boolean isError;
		try {
			result = RMIIndex.bind(request.name, request.ror);
			isError = false;
		} catch(Exception e) {
			result = e;
			isError = true;
		}
		
		return new RMINamingResponse(result, isError);
	}
	
	public RMINamingResponse rebind(RMINamingRequest request) {
		Object result;
		boolean isError;
		try {
			result = RMIIndex.rebind(request.name, request.ror);
			isError = false;
		} catch(Exception e) {
			result = e;
			isError = true;
		}
		
		return new RMINamingResponse(result, isError);
	}
	
	public RMINamingResponse unbind(RMINamingRequest request) {
		Object result;
		boolean isError;
		try {
			result = RMIIndex.unbind(request.name);
			isError = false;
		} catch(Exception e) {
			result = e;
			isError = true;
		}
		
		return new RMINamingResponse(result, isError);
	}
	
	//for testing purposes
	public RemoteObjectReference registerObject(Object o, String interfaceName, String name) throws AlreadyBoundException, NoSuchRemoteObjectReferenceException {
		return RMIIndex.registerObject(o, serverSocket.getHostname(), serverSocket.getPort(), interfaceName, name);
	}
	
	public void registerClass(Class<?> c, String interfaceName) {
		RMIIndex.registerClass(c, interfaceName);
	}
	
	/**
	 * Marshals the lookup request to the RMIIndex and and returns the results as a RMINamingResponse.
	 * @param request
	 * @return
	 */
	public RMINamingResponse lookup(RMINamingRequest request) {
		Object result;
		boolean isError;
		try {
			result = RMIIndex.lookup(request.name);
			System.out.println("Found ROR: "+ ((RemoteObjectReference) result).objectUID);
			isError = false;
		} catch(Exception e) {
			result = e;
			isError = true;
		}
		
		return new RMINamingResponse(result, isError);
	}
	
	/**
	 * Handle unpacks the messages and tries to run the method. And packages and returns the results as a RMIResponse.
	 * Captures errors into the RMIResponse.
	 * @param request
	 * @return
	 */
	public RMIResponse handle(RMIRequest request) {
		Object result;
		boolean isError = true;
		boolean isROR = false;
		RemoteObjectReference ror = request.ror;
		System.out.println("Server.handle: ror: " + ror.objectUID);
		
		try {
			for(int i = 0; request.args != null && i < request.args.length; i++) {
				if(request.remotes[i]) {
					Object a = RMIIndex.getObjectByRor((RemoteObjectReference) request.args[i]);
					if(a != null) {
						//System.out.println("Client passed by reference: Found the Object: "+a);
						request.args[i] = a;
					} else {
						//System.out.println("Client passed by reference: Did NOT find the Object.");
						throw new NoSuchRemoteObjectReferenceException();
					}
				}
				//else {System.out.println("Arg not a remote obj!!");}
			}
			
			if(request.args != null) {
				System.out.println("Args for ClientRequest: "+Util.stringifyArray(request.args));
			}
			
			result = runMethodOn(ror, request.methodName, request.args);
			isError = false;
			if(result != null) {
				if(result instanceof MyRemote) {
					String objectInterfaceName = RMIIndex.getInterfaceNameByClass(result.getClass());
					
					if(objectInterfaceName != null) {
						result = RMIIndex.addObjectAsRor(result, getHostname(), getPort(), objectInterfaceName); //returns an ror
						System.out.println("should be here, created ror: "+result);
						isROR = true;
					}
					else {
						System.out.println("should not be here!!");
						isROR = false;
					}
				}
			}
			
		} catch(Exception e) {
			result = e;
			isError = true;
		}
		
		
		return new RMIResponse(ror, result, isError, isROR);
	}
	
	/**
	 * Tries to run method (methodName) on the remote object (objectUID) with arguments (args).
	 * @param ror
	 * @param methodName
	 * @param args
	 * @return
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */

	public Object runMethodOn(RemoteObjectReference ror, String methodName, Object[] args) throws SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		Object o = RMIIndex.getObjectByRor(ror);
		Class<?> c = o.getClass();
		
		//System.out.println(args.length);
		Class<?>[] argTypes = null;
		if(args != null) {
			argTypes = new Class<?>[args.length];
			for(int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
				System.out.println("Found type: "+argTypes[i].toString());
			}
		}
		
		Method m;
		try {
			m = c.getMethod(methodName, argTypes);
		} catch (NoSuchMethodException e) {
			m = findMethod(c, methodName, args, argTypes);
		}
		
		if(m != null) {
			return m.invoke(o, args);
		}
		else {
			return null;
		}
		
	}
	
	/**
	 * Tries to find a method in the class c with methodName and parameter types of argTypes.
	 * WARNING: It will treat Primitive Wrappers as either primitives or objects, so there are possibilities
	 * for incorrectness due to overloading of functions with similar parameter types.
	 * @param c
	 * @param methodName
	 * @param argTypes
	 * @return
	 * @throws NoSuchMethodException
	 */
	private Method findMethod(Class<?> c, String methodName, Object[] args, Class<?>[] argTypes) throws NoSuchMethodException {
		//System.out.println("Server.findMethod: lets try my own method.");
		Method[] methods = c.getMethods();
		//System.out.println("Server.findMethod: found " + methods.length + " methods.");
		for(int m = 0; m < methods.length; m++) {
			//System.out.println("Server.findMethod: checking method " + methods[m].getName() +" for a match with "+methodName);
			if(methodName.equals(methods[m].getName())) {
				Class<?>[] otherArgTypes = methods[m].getParameterTypes();
				//System.out.println(Util.stringifyArray(otherArgTypes));
				if(argsOfType(args, otherArgTypes)) {return methods[m];}
				
			}
		}
		throw new NoSuchMethodException();
	}
	
	private boolean argsOfType(Object[] args, Class<?>[] argTypes) {
		if(args == null) {
			if(argTypes == null) {return true;}
			else {return false;}
		}
		else if(argTypes == null && args != null) {return false;}
		else {
			if(args.length != argTypes.length) {return false;}
			else {
				for(int a = 0; a < args.length; a++) {
					Class<?> argType = argTypes[a];
					if(argType.isPrimitive()) {
						argType = primToObj.get(argType);
					}
					if(!argType.isInstance(args[a])) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public String getHostname() {
		return serverSocket.getHostname();
	}
	
	public int getPort() {
		return serverSocket.getPort();
	}
}
