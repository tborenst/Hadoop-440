/**
 * A request message sent from a client to a RMI Server to lookup an object with name (name).
 * Author: Vansi Vallabhaneni
 */

package rmi;


public class RMIObjRequest implements RMIMessage {
	private static final long serialVersionUID = -6830740318499414720L;
	public String name;
	
	/**
	 * Constructor for RMIObjReference.
	 * @param name
	 */
	public RMIObjRequest(String name) {
		this.name = name;
	}
}
