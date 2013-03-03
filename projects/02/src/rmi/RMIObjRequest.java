package rmi;

import java.io.Serializable;


public class RMIObjRequest implements Serializable {
	private static final long serialVersionUID = -6830740318499414720L;
	public String name;
	
	public RMIObjRequest(String name) {
		this.name = name;
	}
}
