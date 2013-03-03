package rmi;

import java.io.Serializable;

public abstract class RMIMessage implements Serializable {
	private static final long serialVersionUID = -4374898712470337035L;
	public RemoteObjectReference ror;
	
	public RMIMessage(RemoteObjectReference ror) {
		this.ror = ror;
	}
}
