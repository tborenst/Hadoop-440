package rmi;

public class RMIResponse extends RMIMessage {
	private static final long serialVersionUID = 8578409383104557400L;
	public Object response;
	public boolean isThrowable;
	
	
	
	public RMIResponse(RemoteObjectReference ror, Object response, boolean isThrowable) {
		super(ror);
		this.response = response;
		this.isThrowable = isThrowable;
	}
}
