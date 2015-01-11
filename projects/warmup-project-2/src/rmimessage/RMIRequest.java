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
	public boolean[] remotes;
	
	/**
	 * Constructor for RMIRequest
	 * @param ror
	 * @param method
	 * @param args
	 * @param remotes 
	 */
	public RMIRequest(RemoteObjectReference ror, Method method, Object[] args, boolean[] remotes) {
		this.ror = ror;
		this.methodName = method.getName();
		this.args = args;
		this.remotes = remotes;
	}
}
