/**
 * The Mapper interface (required to be implemented by any class that would run a Map operation).
 * @author Tomer Borenstein
 */
package api;

public interface Mapper {
	public void map(Writable key, Writable value, Collector output);
}
