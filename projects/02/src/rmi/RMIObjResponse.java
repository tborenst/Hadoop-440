package rmi;

import java.io.Serializable;

public class RMIObjResponse implements Serializable {
	private static final long serialVersionUID = 2115010397836118161L;
	public Object response;
	public boolean isThrowable;

	public RMIObjResponse(Object response, boolean isThrowable) {
		this.response = response;
		this.isThrowable = isThrowable;
	}
}
