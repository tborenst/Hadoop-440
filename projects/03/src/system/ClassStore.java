/**
 * The ClassStore class keeps track of the classes that has been loaded on this machine for the current session so 
 * they don't have to be loaded more than once.
 */
package system;

import java.util.HashMap;

public class ClassStore {
	private static HashMap<String, Class<?>> storage = new HashMap<String, Class<?>>();
	
	public static Class<?> getClass(String dir, String file, String name){
		String key = dir + file + name;
		return storage.get(key);
	}
	
	public static void addClass(String dir, String file, String name, Class<?> classObject){
		String key = dir + file + name;
		storage.put(key, classObject);
	}
}
