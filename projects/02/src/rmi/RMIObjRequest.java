package rmi;


public class RMIObjRequest extends RMIMessage {
	private static final long serialVersionUID = -6830740318499414720L;
	public String name;
	
	public RMIObjRequest(RemoteObjectReference ror, String name) {
		super(ror);
		this.name = name;
	}
}
