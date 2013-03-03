/**
 * A response message sent from a RMI Server to a client to return the results from a lookup.
 * If isThrowable is true, the lookup resulted in an error.
 */

package rmi;

import java.io.Serializable;

public class RMIObjResponse implements Serializable {
	private static final long serialVersionUID = 2115010397836118161L;
	public Object response;
	public boolean isThrowable;

	public RMIObjResponse(Object response, boolean isThrowable) {
		this.response = response;
		this.isThrowable = isThrowable;
	}
}
