/**
 * Vansi tests his code here.
 */
package testing;

import java.lang.reflect.*;
import java.util.ArrayList;

public class VansiTest {
	public ArrayList<Object> RMIIndex;
	
	public VansiTest() {
		this.RMIIndex = new ArrayList<Object>();
		this.RMIIndex.add(new Person(1, "tomer"));
	}
	
	
	public void handle(String objectUID, String methodName, Object[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//Object o = RMIIndex.getObject(objectUID);
		Object o = RMIIndex.get(Integer.parseInt(objectUID));
		Class<?> c = o.getClass();
		
		//System.out.println(args.length);
		Class<?>[] types = new Class<?>[args.length];
		for(int i = 0; i < args.length; i++) {
			types[i] = args[i].getClass();
			System.out.println("Found type: "+types[i].toString());
		}
		
		Method m = c.getMethod(methodName, types);
		m.invoke(o, args);
	}
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		VansiTest v = new VansiTest();
		v.handle("0", "toString", new Object[]{});
		v.handle("0", "setName", new Object[]{"doom"});
		v.handle("0", "setAge", new Object[]{10});
	}
}
