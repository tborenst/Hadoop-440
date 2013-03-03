package rmi;


public class RMIObjRequest implements RMIMessage {
	private static final long serialVersionUID = -6830740318499414720L;
	public String objectUID;
	
	public RMIObjRequest(String objectUID) {
		this.objectUID = objectUID;
	}
}
