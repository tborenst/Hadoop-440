package rmi;

import java.rmi.AlreadyBoundException;
import java.util.HashMap;

public class RMIIndex {
	private HashMap<Integer, Object> uidToObj; //objectUID -> object
	private HashMap<String, RemoteObjectReference> nameToRor;
	private HashMap<Class<?>, String> classToInterfaceName;
	
	public RMIIndex(){
		this.uidToObj = new HashMap<Integer, Object>();
		this.nameToRor = new HashMap<String, RemoteObjectReference>();
		this.classToInterfaceName = new HashMap<Class<?>, String>();
	}
	
	public RemoteObjectReference getRorByName(String name){
		synchronized(nameToRor){
			RemoteObjectReference ror = nameToRor.get(name);
			return ror;
		}
	}
	
	public void registerClass(Class<?> c, String interfaceName) {
		synchronized(classToInterfaceName) {
			classToInterfaceName.put(c, interfaceName);
		}
	}
	
	public String getInterfaceNameByClass(Class<?> c) {
		synchronized(classToInterfaceName) {
			return classToInterfaceName.get(c);
		}
	}
	
	/**
	 * Adds an object into the index.
	 * Returns the RemoteObjectReference to the object.
	 * @param o
	 * @param hostname
	 * @param port
	 * @param interfaceName
	 * @param name
	 * @return
	 */
	public RemoteObjectReference registerObject(Object o, String hostname, int port, String interfaceName, String name) {
		RemoteObjectReference ror = addObjectAsRor(o, hostname, port, interfaceName);
		bind(name, ror);
		return ror;
	}
	
	public RemoteObjectReference addObjectAsRor(Object o, String hostname, int port, String interfaceName) {
		RemoteObjectReference ror = new RemoteObjectReference(hostname, port, o.hashCode(), interfaceName);
		synchronized(uidToObj){
			uidToObj.put(ror.objectUID, o);
			return ror;
		}
	}
	
	
	public Object getObjectByRor(RemoteObjectReference ror){
		synchronized(uidToObj){
			Object obj = uidToObj.get(ror.objectUID);
			return obj;
		}
	}
	
	/**
	 * Returns true if successful, false if failed (already bound).
	 */
	public Boolean bind(String name, RemoteObjectReference ror){
		//TODO: need to throw exception (already bound) if name and ror already exist in hashmap
		synchronized(nameToRor){
			if(nameToRor.get(name) == null){
				nameToRor.put(name, ror);
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Returns true if successful, false if failed (no such binding).
	 */
	public Boolean rebind(String name, RemoteObjectReference ror){
		synchronized(nameToRor){
			if(nameToRor.get(name) != null){
				nameToRor.put(name, ror);
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Return true if successful, false if failed (no such bidning).
	 */
	public Boolean unbind(String name, RemoteObjectReference ror){
		synchronized(nameToRor){
			if(nameToRor.get(name) != null){
				nameToRor.remove(name);
				return true;
			} else {
				return false;
			}
		}
	}
}
