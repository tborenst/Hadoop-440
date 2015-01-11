/**
 * A request message sent from a client to a RMI Server to lookup an object with name (name).
 * Author: Vansi Vallabhaneni
 */

package rmimessage;

import rmi.RemoteObjectReference;


public class RMINamingRequest implements RMIMessage {
	private static final long serialVersionUID = -6830740318499414720L;
	public String name;
	public RemoteObjectReference ror;
	
	/**
	 * Constructor for RMIObjReference.
	 * @param name
	 */
	public RMINamingRequest(String name, RemoteObjectReference ror) {
		this.name = name;
		this.ror = ror;
	}
}
