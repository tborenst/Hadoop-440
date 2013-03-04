package myrmi;

import java.io.Serializable;

public class RemoteObjRef implements Serializable{
	private static final long serialVersionUID = 55301626572034814L;
	
	private String hostname;
	private int port;
	private int UID; //unique id
	private String interfaceName;
	
	/**
	 * Remote Object Reference
	 * @param hostname - the hostname of the machine where the real object is located at
	 * @param port - the port at which the machine containing the real object is listening 
	 * @param UID - unique id of ROR
	 * @param interfaceName - name of the interface of the real object this ROR is pointing to
	 */
	public RemoteObjRef(String hostname, int port, int UID, String interfaceName){
		this.hostname = hostname;
		this.port = port;
		this.UID = UID;
		this.interfaceName = interfaceName;
	}
	
	public String getHostname(){
		return hostname;
	}
	
	public int getPort(){
		return port;
	}
	
	public int getUID(){
		return UID;
	}
	
	public String getInterfaceName(){
		return interfaceName;
	}
}
