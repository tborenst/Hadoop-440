/**
 * The Reducer interface (required to be implemented by any class that would run a Reduce operation).
 * @author Tomer Borenstein
 */
package api;

public interface Reducer{
	public void reduce(Writable key, Writable[] values, Collector output);
}
