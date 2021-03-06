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
import networking.SIOCommand;
import networking.SIOServer;


public class ServerHandler {
	public RMIIndex RMIIndex;
	private HashMap<Class<?>, Class<?>> primToObj;
	private SIOServer serverSocket;
	
	/**
	 * Constructor for ServerHandler.
	 * @param port
	 * @param remoteInterface
	 */
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

		/**
		 * SIO Event handlers:
		 */
		serverSocket.on("invokeMethod", new SIOCommand() {
			public void run() {
				RMIRequest requestData = (RMIRequest) object;
				RMIResponse response = handle(requestData); //arg0 = RMIRequest
				socket.respond(requestId, response);
			}
		});
		
		serverSocket.on("lookupObject", new SIOCommand() {
			public void run() {
				RMINamingResponse response = lookup((RMINamingRequest) object);
				socket.respond(requestId, response);
			}
		});
		
		serverSocket.on("bindObject", new SIOCommand() {
			public void run() {
				RMINamingResponse response = bind((RMINamingRequest) object);
				socket.respond(requestId, response);
			}
		});
		
		serverSocket.on("rebindObject", new SIOCommand() {
			public void run() {
				RMINamingResponse response = rebind((RMINamingRequest) object);
				socket.respond(requestId, response);
			}
		});
		
		serverSocket.on("unbindObject", new SIOCommand() {
			public void run() {
				RMINamingResponse response = unbind((RMINamingRequest) object);
				if(socket != null && socket.isAlive()) socket.respond(requestId, response);
			}
		});
	}
	
	/**
	 * Binds the specified name to a remote object.
	 * @param request
	 * @return
	 */
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
	
	/**
	 * Rebinds the specified name to a new remote object. Any existing binding for the name is replaced.
	 * @param request
	 * @return
	 */
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
	
	/**
	 * Destroys the binding for the specified name that is associated with a remote object.
	 * @param request
	 * @return
	 */
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
	
	/**
	 * Adds the object to the server.
	 * FOR ASSIGNMENT'S TESTING PURPOSES ONLY.
	 * @param o
	 * @param interfaceClass
	 * @param name
	 * @return
	 * @throws AlreadyBoundException
	 * @throws NoSuchRemoteObjectReferenceException
	 */
	public RemoteObjectReference registerObject(Object o, Class<?> interfaceClass, String name) 
		   throws AlreadyBoundException, NoSuchRemoteObjectReferenceException {
		return RMIIndex.registerObject(o, serverSocket.getHostname(), serverSocket.getPort(), interfaceClass.getSimpleName(), name);
	}
	
	/**
	 * Register the class and interfaceName (the interface c implements and is casted to on the client side).
	 * @param objectClass - the class to be register
	 * @param interfaceClass - the interface to be registered with the class
	 */
	public void registerClass(Class<?> objectClass, Class<?> interfaceClass) {
		RMIIndex.registerClass(objectClass, interfaceClass.getSimpleName());
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
		
		try {
			for(int i = 0; request.args != null && i < request.args.length; i++) {
				if(request.remotes[i]) {
					Object a = RMIIndex.getObjectByRor((RemoteObjectReference) request.args[i]);
					if(a != null) {
						request.args[i] = a;
					} else {
						throw new NoSuchRemoteObjectReferenceException();
					}
				}
			}
			
			result = runMethodOn(ror, request.methodName, request.args);
			isError = false;
			if(result != null) {
				if(result instanceof MyRemote) {
					String objectInterfaceName = RMIIndex.getInterfaceNameByClass(result.getClass());
					if(objectInterfaceName != null) {
						result = RMIIndex.addObjectAsRor(result, getHostname(), getPort(), objectInterfaceName); //returns an ror
						isROR = true;
					}
					else {
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
		
		Class<?>[] argTypes = null;
		if(args != null) {
			argTypes = new Class<?>[args.length];
			for(int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
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
		Method[] methods = c.getMethods();
		for(int m = 0; m < methods.length; m++) {
			if(methodName.equals(methods[m].getName())) {
				Class<?>[] otherArgTypes = methods[m].getParameterTypes();
				if(argsOfType(args, otherArgTypes)) {return methods[m];}
				
			}
		}
		throw new NoSuchMethodException();
	}
	
	/**
	 * Checks if the array of args is of type described in argTypes.
	 * @param args
	 * @param argTypes
	 * @return
	 */
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
	
	/**
	 * Returns the hostname of the server.
	 * @return
	 */
	public String getHostname() {
		return serverSocket.getHostname();
	}
	
	/**
	 * Returns the port of the server's socket.
	 * @return
	 */
	public int getPort() {
		return serverSocket.getPort();
	}
}
