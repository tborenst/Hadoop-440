/**
 * The Reducer interface (required to be implemented by any class that would run a Reduce operation).
 * @author Tomer Borenstein
 */
package api;

import java.util.Iterator;

public interface Reducer <KeyWritable, ValueWritable>{
	public void reduce(KeyWritable key, Iterator<ValueWritable> values, Collector output);
}
