/**
 * A response message sent from a RMI Server to a client to return the results from a lookup.
 * If isThrowable is true, the lookup resulted in an error.
 * Author: Vansi Vallabhaneni
 */

package rmimessage;

public class RMINamingResponse implements RMIMessage {
	private static final long serialVersionUID = 2115010397836118161L;
	public Object response;
	public boolean isError;

	/**
	 * Constructor for RMINamingResponse.
	 * @param response
	 * @param isThrowable
	 */
	public RMINamingResponse(Object response, boolean isError) {
		this.response = response;
		this.isError = isError;
	}
}
