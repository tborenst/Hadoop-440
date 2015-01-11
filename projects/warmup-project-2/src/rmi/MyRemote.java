/**
 * This interface needs to be implemented by Remote Objects.
 * Author: Vansi Vallabaneni.
 */

package rmi;

import java.io.Serializable;

public interface MyRemote extends Serializable{
	public RemoteObjectReference getROR();
}
