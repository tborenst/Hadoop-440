/**
 * The Executer class can be used to load other classes, instantiate them, and invoke methods on them.
 * @author Tomer Borenstein
 */

package util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Executer extends ClassLoader{
	/**
	 * getClass - returns a class object given a path to a .class file, and a class's binary name.
	 * @param dir - absolute path to dir containing .class file (e.g. "User/Desktop/projects/bin/tests/")
	 * @param fileName - name of .class file (e.g. "Test.class")
	 * @param className - binary name of the class (e.g. for a Test class in package tests: "tests.Test")
	 */
	public Class<?> getClass(String dir, String fileName, String className) throws IOException {
		byte[] code = Files.readAllBytes(Paths.get(dir, fileName));
		return defineClass(className, code, 0, code.length);
	}

	/**
	 * instantiate - given a class and a set of arguments (an object array or null), returns an instantiated copy of the class.
	 * If an error occurs, this method will return null.
	 * WARNING: cannot handle constructors that take in primitives as arguments (use analogous classes instead).
	 */
	public Object instantaite(Class<?> classObject, Object[] args) {
		try{
			if(args == null){
				Constructor<?> constructor = classObject.getConstructor();
				return constructor.newInstance();
			} else {
				//create an array with the types of objects
				Class<?>[] argtypes = new Class<?>[args.length];
				for(int i = 0; i < argtypes.length; i++){
					argtypes[i] = args[i].getClass();
				}
				Constructor<?> constructor = classObject.getConstructor(argtypes);
				return constructor.newInstance(args);
			}
		} catch (Exception e){
			//TODO: remove debugging line
			System.out.println("Failed in: Executer.instantiate()");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * execute - given an object, a method name, an a set of arguments (an object array or null), invokes the method on the given 
	 * object. This method returns whatever the invoked method returns.
	 * If an error occurs, this method will return null.
	 * WARNING: cannot handle methods that take in primitives as arguments (use analogous classes instead).
	 */
	public Object execute(Object obj, String methodName, Object[] args){
		try{
			if(args == null){
				Class<?> myClass = obj.getClass();
				Method myMethod = myClass.getMethod(methodName, null);
				return myMethod.invoke(obj, null);
			} else {
				//create an array with the types of objects
				Class<?>[] argtypes = new Class<?>[args.length];
				for(int i = 0; i < argtypes.length; i++){
					argtypes[i] = args[i].getClass();
				}
				Class<?> myClass = obj.getClass();
				Method myMethod = myClass.getMethod(methodName, argtypes);
				return myMethod.invoke(obj, args);
			}
		} catch (Exception e){
			//TODO: remove debuggling line
			System.out.println("Failed in: Executer.execute()");
			e.printStackTrace();
			return null;
		}
	}
	
//	public Object execute(Object obj, String methodName, Object[] args){
//		try{
//			Class<?> myClass = obj.getClass();
//			
//			if(args == null){
//				Method myMethod = myClass.getMethod(methodName, null);
//				return myMethod.invoke(obj, null);
//			} else {
//				Method[] methods = myClass.getMethods();
//				for(int i = 0; i < methods.length; i++){
//					Method method = methods[i];
//					if(method.getName().equals(methodName)){
//						Class<?>[] paramtypes = method.getParameterTypes();
//						if(paramtypes.length == args.length){
//							Class<?>[] argtypes = new Class<?>[args.length];
//							boolean wrongMethod = false;
//							for(int j = 0; (j < args.length && !wrongMethod); j++){
//								if(!(paramtypes[i].isInstance(args[i]))){
//									wrongMethod = true;
//								}
//							}
//							if(!wrongMethod){
//								for(int k = 0; k < args.length; k++){
//									if(paramtypes[k].isArray()){
//										Object[] a = (Object[])args[k];
//										Class<?> simp = paramtypes[k].getComponentType();
//										for(int m = 0; m < a.length; m++){
//											a[m] = simp.cast(a[m]);
//										}
//										args[k] = a;
//									} else {
//										args[k] = paramtypes[k].cast(args[k]);
//									}
//								}
//								return method.invoke(obj, args);
//							}
//						}
//					}
//					throw new Exception("shit fuck!");
//				}
//			}
//		} catch (Exception e){
//			e.printStackTrace();
//			return null;
//		}
//		return null;
//	}
	
}
