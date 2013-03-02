package testing;

public class RemoteObjectReference {
	public String hostname;
	public int port;
	public String objectUID;
	
	public RemoteObjectReference(String hostname, int port, String objectUID) {
		this.hostname = hostname;
		this.port = port;
		this.objectUID = objectUID;
	}
}
