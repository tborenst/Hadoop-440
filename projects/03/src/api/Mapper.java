/**
 * The Mapper interface (required to be implemented by any class that would run a Map operation).
 * @author Tomer Borenstein
 */
package api;

public interface Mapper <KeyType, ValueType>{
	public void map(KeyType key, ValueType value, Collector output);
}
