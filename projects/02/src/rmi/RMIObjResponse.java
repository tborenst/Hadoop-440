package rmi;

public class RMIObjResponse extends RMIMessage{
	private static final long serialVersionUID = 2115010397836118161L;

	public RMIObjResponse(RemoteObjectReference ror) {
		super(ror);
	}
}
