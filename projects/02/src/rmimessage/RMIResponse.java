/**
 * A response message from a RMI Server to a client to return the result from a RMIRequest.
 * If isThrowable is true, the request resulted in an error.
 * Author: Vansi Vallabhaneni
 */

package rmimessage;

import rmi.RemoteObjectReference;

public class RMIResponse implements RMIMessage {
	private static final long serialVersionUID = 8578409383104557400L;
	public Object response;
	public boolean isError;
	public RemoteObjectReference ror;
	public boolean isROR;
	
	/**
	 * Constructor for RMIResponse.
	 * @param ror
	 * @param response
	 * @param isThrowable
	 */
	public RMIResponse(RemoteObjectReference ror, Object response, boolean isError, boolean isROR) {
		this.ror = ror;
		this.response = response;
		this.isError = isError;
		this.isROR = isROR;
	}
}
