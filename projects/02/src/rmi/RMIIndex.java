package rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
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
	 * @throws AlreadyBoundException 
	 * @throws NoSuchRemoteObjectReferenceException 
	 */
	public RemoteObjectReference registerObject(Object o, String hostname, int port, String interfaceName, String name) throws AlreadyBoundException, NoSuchRemoteObjectReferenceException {
		RemoteObjectReference ror = addObjectAsRor(o, hostname, port, interfaceName);
		bind(name, ror);
		return ror;
	}
	
	public RemoteObjectReference addObjectAsRor(Object o, String hostname, int port, String interfaceName) {
		RemoteObjectReference ror = new RemoteObjectReference(hostname, port, o.hashCode(), interfaceName);
		synchronized(uidToObj){
			uidToObj.put(ror.objectUID, o);
		}
		return ror;
	}
	
	
	public Object getObjectByRor(RemoteObjectReference ror){
		synchronized(uidToObj){
			Object obj = uidToObj.get(ror.objectUID);
			return obj;
		}
	}
	
	public Object lookup(String name) throws NotBoundException {
		Object o = getRorByName(name);
		if(o == null) {
			throw new NotBoundException();
		}
		return o;
	}
	
	/**
	 * Bind a ROR to a particular name.
	 * If the ROR does not exist on this registry throws 
	 * If the name is already taken, this method throws AlreadyBoundException
	 * @throws AlreadyBoundException 
	 * @throws NoSuchRemoteObjectReferenceException 
	 */
	public Boolean bind(String name, RemoteObjectReference ror) throws AlreadyBoundException, NoSuchRemoteObjectReferenceException {
		synchronized(nameToRor){
			synchronized(uidToObj) {
				if(ror == null  || uidToObj.get(ror.objectUID) == null) {
					throw new NoSuchRemoteObjectReferenceException();
				}
				else {
					if(nameToRor.get(name) == null){
						nameToRor.put(name, ror);
						return true;
					} else {
						throw new AlreadyBoundException();
					}
				}
			}
		}
	}
	
	/**
	 * Returns true if successful, false if failed (no such binding).
	 * @throws NoSuchRemoteObjectReferenceException 
	 */
	public Boolean rebind(String name, RemoteObjectReference ror) throws NoSuchRemoteObjectReferenceException{
		synchronized(nameToRor){
			synchronized(uidToObj) {
				if(ror == null || uidToObj.get(ror.objectUID) == null) {
					throw new NoSuchRemoteObjectReferenceException();
				}
				else {
					nameToRor.put(name, ror);
					return true;
				}
			}
		}
	}
	

	/**
	 * Return true if successful, false if failed (no such bidning).
	 * @throws NotBoundException 
	 */
	public Boolean unbind(String name) throws NotBoundException{
		synchronized(nameToRor){
			if(nameToRor.get(name) != null){
				nameToRor.remove(name);
				return true;
			} else {
				throw new NotBoundException();
			}
		}
	}
}
