/**
 * The MigratableProcess interface allows classes that implement it to be run by threads
 * and to be serialized.
 */
package migratableProcesses;

import java.io.Serializable;

public interface MigratableProcess extends Runnable, Serializable{
	/**
	 * void Suspend(void):
	 * Allows the object to enter a safe state before it is serialized.
	 */
	public void suspend();
}
