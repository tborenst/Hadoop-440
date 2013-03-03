package rmi;

import java.rmi.AlreadyBoundException;
import java.util.HashMap;

public class RMIIndex {
	private HashMap<RemoteObjectReference, Object> rorToObj;
	private HashMap<String, RemoteObjectReference> nameToRor;
	
	public RMIIndex(){
		this.rorToObj = new HashMap<RemoteObjectReference, Object>();
		this.nameToRor = new HashMap<String, RemoteObjectReference>();
	}
	
	public RemoteObjectReference getRorByName(String name){
		synchronized(nameToRor){
			RemoteObjectReference ror = nameToRor.get(name);
			return ror;
		}
	}
	
	public Object getObjectByRor(RemoteObjectReference ror){
		synchronized(rorToObj){
			Object obj = rorToObj.get(ror);
			return obj;
		}
	}
	
	/**
	 * Returns true if successful, false if failed (already bound).
	 */
	public Boolean bind(String name, RemoteObjectReference ror){
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
