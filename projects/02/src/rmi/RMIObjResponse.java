/**
 * A response message sent from a RMI Server to a client to return the results from a lookup.
 * If isThrowable is true, the lookup resulted in an error.
 * Author: Vansi Vallabhaneni
 */

package rmi;

public class RMIObjResponse implements RMIMessage {
	private static final long serialVersionUID = 2115010397836118161L;
	public Object response;
	public boolean isError;

	/**
	 * Constructor for RMIObjResponse.
	 * @param response
	 * @param isThrowable
	 */
	public RMIObjResponse(Object response, boolean isError) {
		this.response = response;
		this.isError = isError;
	}
}
