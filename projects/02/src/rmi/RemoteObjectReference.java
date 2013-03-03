package rmi;

public class RemoteObjectReference {
	public String hostname;
	public int port;
	public String objectUID;
	public String interfaceName;
	
	public RemoteObjectReference(String hostname, int port, String objectUID, String interfaceName) {
		this.hostname = hostname;
		this.port = port;
		this.objectUID = objectUID;
		this.interfaceName = interfaceName;
	}
}
