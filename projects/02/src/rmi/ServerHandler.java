package rmi;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;

import networking.SIOCommand;
import networking.SIOServer;

import vansitest.PersonImpl;

public class ServerHandler {
	public ArrayList<Object> RMIIndex;
	private HashMap<Class<?>, Class<?>> primToObj;
	private SIOServer serverSocket;
	
	public ServerHandler(int port) {
		this.RMIIndex = new ArrayList<Object>();
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
				RMIResponse response = handle((RMIRequest) object); //arg0 = RMIRequest
				socket.respond(requestId, response);
			}
		});
		
		/*serverSocket.on("lookupObject", new SIOCommand() {
			public void run() {
				RMIResponse response = lookup((RMIObjRequest) object);
				socket.respond(requestId, response);
			}
		});*/
	}
	
	/*public RMIResponse lookup(RMIObjRequest request) {
		Object result;
		boolean isThrowable;
		RemoteObjectReference ror = request.ror;
		try {
			result = RMIIndex.getROR(request.name);
			isThrowable = false;
		} catch(Exception e) {
			result = e;
			isThrowable = true;
		}
		
		return new RMIResponse(ror, result, isThrowable);
		
	}*/
	
	public RMIResponse handle(RMIRequest request) {
		Object result;
		boolean isThrowable;
		RemoteObjectReference ror = request.ror;
		try {
			result = runMethodOn(ror.objectUID, request.methodName, request.args);
			isThrowable = false;
		} catch(Exception e) {
			result = e;
			isThrowable = true;
		}
		
		return new RMIResponse(ror, result, isThrowable);
		
	}
	
	
	public Object runMethodOn(String objectUID, String methodName, Object[] args) throws SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		//Object o = RMIIndex.getObject(objectUID);
		Object o = RMIIndex.get(Integer.parseInt(objectUID));
		Class<?> c = o.getClass();
		
		//System.out.println(args.length);
		Class<?>[] argTypes = new Class<?>[args.length];
		for(int i = 0; i < args.length; i++) {
			argTypes[i] = args[i].getClass();
			System.out.println("Found type: "+argTypes[i].toString());
		}
		
		Method m;
		try {
			m = c.getMethod(methodName, argTypes);
		} catch (NoSuchMethodException e) {
			m = findMethod(c, argTypes);
		}
		
		if(m != null) {
			return m.invoke(o, args);
		}
		else {
			return null;
		}
		
	}
	
	private Method findMethod(Class<?> c, Class<?>[] argTypes) throws NoSuchMethodException {
		System.out.println("lets try my own method");
		Method[] methods = c.getMethods();
		for(int m = 0; m < methods.length; m++) {
			Class<?>[] otherArgTypes = methods[m].getParameterTypes();
			//System.out.println(Util.stringifyArray(otherArgTypes));
			if(typesArrayEqual(argTypes, otherArgTypes)) {return methods[m];}
		}
		throw new NoSuchMethodException();
	}
	
	private boolean typesArrayEqual(Class<?>[] t1Arr, Class<?>[] t2Arr) {
		if(t1Arr.length != t2Arr.length) {return false;}
		
		for(int i = 0; i < t1Arr.length; i++) {
			//System.out.println("comparing: "+t1Arr[i].toString()+" & "+t2Arr[i].toString()+" -> "+t1Arr[i].equals(t2Arr[i]));
			if(!typeEqual(t1Arr[i], t2Arr[i])) {return false;}
		}
		return true;
	}
	
	private boolean typeEqual(Class<?> t1, Class<?> t2) {
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
	
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ServerHandler v = new ServerHandler(8080);
		v.RMIIndex.add(new PersonImpl(1, "tomer"));
		v.runMethodOn("0", "toString", new Object[]{});
		v.runMethodOn("0", "setName", new Object[]{"doom"});
		v.runMethodOn("0", "setAge", new Object[]{10});
		
		
		
		/*
		System.out.println("----------");
		
		Object[] methodArgs = new Object[]{10};
		Class<?>[] argTypes = new Class<?>[methodArgs.length];
		for(int i = 0; i < methodArgs.length; i++) {
			argTypes[i] = methodArgs[i].getClass();
			System.out.println("2: Found type: "+argTypes[i].toString());
		}
		
		Object obj = v.RMIIndex.get(0);
		Class<?> c = obj.getClass();
		Method method = c.getMethod("setAge", argTypes);
		Class<?>[] otherArgTypes = method.getParameterTypes();
		for(int o = 0; o < otherArgTypes.length; o++) {
			System.out.println(otherArgTypes[o].toString()+" "+otherArgTypes[o].equals(argTypes[o]));
		}
		
		System.out.println(Arrays.equals(argTypes, otherArgTypes));
		*/	
	}
}
