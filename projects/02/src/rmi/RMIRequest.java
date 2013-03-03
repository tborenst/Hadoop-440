/**
 * A request message from a client to a RMI Server to invoke a method (methodName) 
 * on the remote object (with objectUID) with arguments (args).
 */
package rmi;

import java.lang.reflect.Method;


public class RMIRequest extends RMIMessage {
	private static final long serialVersionUID = -6830740318499414720L;
	public String objectUID;
	public RemoteObjectReference ror;
	public String methodName;
	public Object[] args;
	
	public RMIRequest(RemoteObjectReference ror, Method method, Object[] args) {
		super(ror);
		this.methodName = method.getName();
		this.args = args;
	}
}
