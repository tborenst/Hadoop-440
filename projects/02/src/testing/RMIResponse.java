package testing;

public class RMIResponse implements RMIMessage {
	private static final long serialVersionUID = 8578409383104557400L;
	public Object response;
	public boolean isThrowable;
	
	
	
	public RMIResponse(Object response, boolean isThrowable) {
		this.response = response;
		this.isThrowable = isThrowable;
	}
}
