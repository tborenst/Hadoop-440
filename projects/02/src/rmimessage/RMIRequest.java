/**
 * A request message from a client to a RMI Server to invoke a method (methodName) 
 * on the remote object (with objectUID) with arguments (args).
 * Author: Vansi Vallabhaneni
 */
package rmimessage;

import java.lang.reflect.Method;

import rmi.RemoteObjectReference;


public class RMIRequest implements RMIMessage {
	private static final long serialVersionUID = -6830740318499414720L;
	public String objectUID;
	public RemoteObjectReference ror;
	public String methodName;
	public Object[] args;
	
	/**
	 * Constructor for RMIRequest
	 * @param ror
	 * @param method
	 * @param args
	 */
	public RMIRequest(RemoteObjectReference ror, Method method, Object[] args) {
		this.ror = ror;
		this.methodName = method.getName();
		this.args = args;
	}
}
