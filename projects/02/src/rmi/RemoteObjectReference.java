/**
 * A transparent object containing all necessary the identifying information for a remote object.
 * Author: Vansi Vallabhaneni
 */

package rmi;

public class RemoteObjectReference {
	public String hostname;
	public int port;
	public String objectUID;
	public String interfaceName;
	
	/**
	 * Constructor for RemoteObjectReference
	 * @param hostname
	 * @param port
	 * @param objectUID
	 * @param interfaceName
	 */
	public RemoteObjectReference(String hostname, int port, String objectUID, String interfaceName) {
		this.hostname = hostname;
		this.port = port;
		this.objectUID = objectUID;
		this.interfaceName = interfaceName;
	}
}
