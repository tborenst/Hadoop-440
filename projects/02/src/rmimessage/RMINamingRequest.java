/**
 * A request message sent from a client to a RMI Server to lookup an object with name (name).
 * Author: Vansi Vallabhaneni
 */

package rmimessage;


public class RMINamingRequest implements RMIMessage {
	private static final long serialVersionUID = -6830740318499414720L;
	public String name;
	
	/**
	 * Constructor for RMIObjReference.
	 * @param name
	 */
	public RMINamingRequest(String name) {
		this.name = name;
	}
}
