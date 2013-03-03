/**
 * Abstract class for RMI messages relating to remote objects. 
 * Therefore every such message needs to include a RemoteObjectReference.
 * Author: Vansi Vallabhaneni
 */

package rmi;

import java.io.Serializable;

public abstract class RMIMessage implements Serializable {
	private static final long serialVersionUID = -4374898712470337035L;
	public RemoteObjectReference ror;
	
	/**
	 * Constructor for RMIMessage.
	 * @param ror
	 */
	public RMIMessage(RemoteObjectReference ror) {
		this.ror = ror;
	}
}
