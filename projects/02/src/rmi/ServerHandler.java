/**
 * ServerHandler is the interface through which the requests are marshaled and 
 * methods invoked after which results are sent back to the requesters.
 * Author: Vansi Vallabhaneni
 */

package rmi;

import java.lang.reflect.*;
import java.util.HashMap;

import vansitest.Util;

import networking.SIOCommand;
import networking.SIOServer;


public class ServerHandler {
	public RMIIndex RMIIndex;
	private HashMap<Class<?>, Class<?>> primToObj;
	private SIOServer serverSocket;
	
	public ServerHandler(int port) {
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
				System.out.println("Server: recieved a lookupObject requerst.");
				RMIObjResponse response = lookup((RMIObjRequest) object);
				System.out.println("Responding: error=" + response.isThrowable);
				socket.respond(requestId, response);
			}
		});
	}
	
	//for testing purposes
	public RemoteObjectReference addObject(Object o, String interfaceName, String name) {
		return RMIIndex.addObject(o, serverSocket.getHostname(), serverSocket.getPort(), interfaceName, name);
	}
	
	/**
	 * Marshals the lookup request to the RMIIndex and and returns the results as a RMIObjResponse.
	 * @param request
	 * @return
	 */
	public RMIObjResponse lookup(RMIObjRequest request) {
		Object result;
		boolean isThrowable;
		try {
			result = RMIIndex.getRorByName(request.name);
			System.out.println("Found ROR: "+ ((RemoteObjectReference) result).objectUID);
			isThrowable = false;
		} catch(Exception e) {
			result = e;
			isThrowable = true;
		}
		
		return new RMIObjResponse(result, isThrowable);
		
	}
	
	/**
	 * Handle unpacks the messages and tries to run the method. And packages and returns the results as a RMIResponse.
	 * Captures errors into the RMIResponse.
	 * @param request
	 * @return
	 */
	public RMIResponse handle(RMIRequest request) {
		Object result;
		boolean isThrowable;
		RemoteObjectReference ror = request.ror;
		System.out.println("Server.handle: ror: " + ror.objectUID);
		try {
			result = runMethodOn(ror, request.methodName, request.args);
			isThrowable = false;
		} catch(Exception e) {
			result = e;
			isThrowable = true;
		}
		
		return new RMIResponse(ror, result, isThrowable);
		
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
			m = findMethod(c, methodName, argTypes);
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
	private Method findMethod(Class<?> c, String methodName, Class<?>[] argTypes) throws NoSuchMethodException {
		//System.out.println("Server.findMethod: lets try my own method.");
		Method[] methods = c.getMethods();
		//System.out.println("Server.findMethod: found " + methods.length + " methods.");
		for(int m = 0; m < methods.length; m++) {
			//System.out.println("Server.findMethod: checking method " + methods[m].getName() +" for a match with "+methodName);
			if(methodName.equals(methods[m].getName())) {
				Class<?>[] otherArgTypes = methods[m].getParameterTypes();
				//System.out.println(Util.stringifyArray(otherArgTypes));
				if(typesArrayEqual(argTypes, otherArgTypes)) {return methods[m];}
			}
		}
		throw new NoSuchMethodException();
	}
	
	/**
	 * Checks if every element in the type arrays are equal.
	 * @param t1Arr
	 * @param t2Arr
	 * @return
	 */
	private boolean typesArrayEqual(Class<?>[] t1Arr, Class<?>[] t2Arr) {
		if(t1Arr.length != t2Arr.length) {return false;}
		
		for(int i = 0; i < t1Arr.length; i++) {
			System.out.println("comparing: " + t1Arr[i].toString() +" & " + t2Arr[i].toString() 
									+ " -> " + typeEqual(t1Arr[i], t2Arr[i]));
			if(!typeEqual(t1Arr[i], t2Arr[i])) {return false;}
		}
		return true;
	}
	
	/**
	 * Checks if the types are equal.
	 * WARNING: treats primitive wrappers as primitives and objects when trying to test for equality
	 * (e.g typeEqual(int.class, Integer.class) is true).
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean typeEqual(Class<?> t1, Class<?> t2) {
		if(t1.equals(t2)) {
			return true;
		}
		else {
			Class<?> primitive = t1;
			Class<?> object = t2;
			if(t2.isPrimitive()) {
				primitive = t2;
				object = t1;
			}
			else if(!t1.isPrimitive()) {return false;}
			
			if(primToObj.containsKey(primitive)) {
				return object.equals(primToObj.get(primitive));
			}
			else {
				return false;
			}
				
		}
	}
	


	public String getHostname() {
		return serverSocket.getHostname();
	}
	
	public int getPort() {
		return serverSocket.getPort();
	}
	
	//testing
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ServerHandler s = new ServerHandler(8080);
		System.out.println(s.typeEqual(Integer.class, int.class));
	}
}
