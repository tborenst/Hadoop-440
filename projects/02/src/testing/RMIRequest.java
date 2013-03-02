package testing;

import java.io.Serializable;
import java.lang.reflect.Method;


public class RMIRequest implements RMIMessage {
	private static final long serialVersionUID = -6830740318499414720L;
	public String objectUID;
	public String methodName;
	public Object[] args;
	
	public RMIRequest(String objectUID, Method method, Object[] args) {
		this.objectUID = objectUID;
		this.methodName = method.getName();
		this.args = args;
	}
}
