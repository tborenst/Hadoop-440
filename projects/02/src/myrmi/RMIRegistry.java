package myrmi;

import java.util.HashMap;

public class RMIRegistry {
	private HashMap<Integer, Object> rorIdToObj;
	private HashMap<String, RemoteObjRef> nameToRor;
	private String hostname;
	private int port;
	private int rorCount;
	
	public RMIRegistry(String hostname, int port){
		this.rorIdToObj = new HashMap<Integer, Object>();
		this.nameToRor = new HashMap<String, RemoteObjRef>();
		this.hostname = hostname;
		this.port = port;
		this.rorCount = 0;
	}
	
	//methods to be used by the RMI server
	
	/**
	 * Register a new object as a first point of contact for clients.
	 * This method will create a unique ROR for the object.
	 * @param name - the name to lookup to get this object
	 * @param obj - the object to be registered
	 * @param interfaceName - the name of the interface to register the object with
	 * @return - true if successful, false if failed
	 */
	public Boolean register(String name, Object obj, String interfaceName){
		synchronized(nameToRor){
			if(nameToRor.get(name) != null){
				//name taken
				return false;
			} else {
				//name not taken, create new ROR and register
				synchronized(rorIdToObj){
					int id = rorCount++;
					RemoteObjRef ror = new RemoteObjRef(hostname, port, id, interfaceName);
					nameToRor.put(name, ror);
					rorIdToObj.put(id, obj);
					return true;
				}
			}
		}
	}
	
	/**
	 * Add a new object as a remote object, but not as a first point of contact for RMI clients.
	 * This method simply creates a new ROR for an object and throws it in the ROR pool, but it will
	 * not be associated with a name and will not be able to be looked up by clients.
	 * @param obj - the object to be added to the ROR pool
	 * @param interfaceName - the name of the interface to register the object with
	 * @return ror - the RemoteObjRef created for this object
	 */
	public RemoteObjRef addObjectAsRor(Object obj, String interfaceName){
		synchronized(rorIdToObj){
			//create a new ROR for this object and throw it in the ROR pool
			int id = rorCount++;
			RemoteObjRef ror = new RemoteObjRef(hostname, port, id, interfaceName);
			rorIdToObj.put(id,  obj);
			return ror;
		}
	}
	
	/**
	 * Get a reference to a local (to the server) object by ROR.
	 * @param ror - the RemoteObjRef to the desired local object
	 * @return obj - the desired local object if found, null if not found
	 */
	public Object getObjectByRor(RemoteObjRef ror){
		synchronized(rorIdToObj){
			int id = ror.getUID();
			return rorIdToObj.get(id);
		}
	}
	
	//methods to be used by requests from the client
	
	/**
	 * Look up a ROR by name.
	 * @param name - name of service
	 * @return ror - if found, null if no such service name exists
	 */
	public RemoteObjRef lookupRorByName(String name){
		synchronized(nameToRor){
			return nameToRor.get(name);
		}
	}
	
	/**
	 * Bind a ROR to a particular name.
	 * If the ROR does not exist on this registry, or if the name is already taken, this method
	 * will return false. It will return true on success.
	 * @param name - the name to be bound
	 * @param ror - the RemoteObjRef to be bound to the name
	 * @return - true if successful, false otherwise
	 */
	public Boolean bind(String name, RemoteObjRef ror){
		synchronized(nameToRor){
			synchronized(rorIdToObj){
				int id = ror.getUID();
				if(rorIdToObj.get(id) == null){
					//no such ROR in the ROR pool
					return false;
				}
				if(nameToRor.get(name) != null){
					//name taken
					return false;
				}
				nameToRor.put(name, ror);
				return true;
			}
		}
	}
	
	/**
	 * Re-bind an existing name and a ROR.
	 * If the ROR does not exist on this registry, this method will return false. If the name is not yet
	 * bound, this method will create a new binding and return true.
	 * @param name - the name to be bound
	 * @param ror - the RemoteObjRef to be bound to the name
	 * @return - true if successful, false otherwise
	 */
	public Boolean rebind(String name, RemoteObjRef ror){
		synchronized(nameToRor){
			synchronized(rorIdToObj){
				int id = ror.getUID();
				if(rorIdToObj.get(id) == null){
					//no such ROR in the ROR pool
					return false;
				}
				nameToRor.put(name, ror);
				return true;
			}
		}
	}
	
	/**
	 * Un-bind a name.
	 * If the name does not exist or the method will return false. It will return true otherwise.
	 * @param name - the name to be unbound
	 * @return - true if the name was successfully unbound, false if the name didn't exist
	 */
	public Boolean unbind(String name){
		synchronized(nameToRor){
			if(nameToRor.get(name) == null){
				//name does not exist
				return false;
			} else {
				nameToRor.remove(name);
				return true;
			}
		}
	}
}
