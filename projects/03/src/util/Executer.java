/**
 * The Executer class can be used to load other classes, instantiate them, and invoke methods on them.
 * @author Tomer Borenstein
 */

package util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
//import java.nio.file.Files;
//import java.nio.file.Paths;

import system.ClassStore;


public class Executer extends ClassLoader{
	/**
	 * getClass - returns a class object given a path to a .class file, and a class's binary name.
	 * @param dir - absolute path to dir containing .class file (e.g. "User/Desktop/projects/bin/tests/")
	 * @param fileName - name of .class file (e.g. "Test.class")
	 * @param className - binary name of the class (e.g. for a Test class in package tests: "tests.Test")
	 * @throws IOException 
	 */
	public Class<?> getClass(String dir, String fileName, String className) throws IOException {
		//byte[] code = Files.readAllBytes(Paths.get(dir, fileName));
		
		File f = new File(dir + "/" + fileName);
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		byte[] code = new byte[(int) raf.length()];
		raf.read(code);
		
		Class<?> classObject;
		classObject = ClassStore.getClass(dir, fileName, className);
		if(classObject == null){
			classObject = defineClass(className, code, 0, code.length);
			ClassStore.addClass(dir, fileName, className, classObject);
		}
		return classObject;
	}

	/**
	 * instantiate - given a class and a set of arguments (an object array or null), returns an instantiated copy of the class.
	 * If an error occurs, this method will return null.
	 * WARNING: cannot handle constructors that take in primitives as arguments (use analogous classes instead).
	 * @throws Exception 
	 */
	public Object instantaite(Class<?> classObject, Object[] args) throws Exception {
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
			throw e;
		}
	}
	
	/**
	 * execute - given an object, a method name, an a set of arguments (an object array or null), invokes the method on the given 
	 * object. This method returns whatever the invoked method returns.
	 * If an error occurs, this method will return null.
	 * WARNING: cannot handle methods that take in primitives as arguments (use analogous classes instead).
	 * @throws Exception 
	 */	
	public Object execute(Object obj, String methodName, Object[] args) throws Exception{
		try{
			if(args == null){
				Class<?> myClass = obj.getClass();
				Method myMethod = myClass.getMethod(methodName);
				return myMethod.invoke(obj);
			} else {
				Class<?> myClass = obj.getClass();
				Method[] methods = myClass.getMethods();
				for(int i = 0; i < methods.length; i++){
					Method method = methods[i];
					if(method.getName().equals(methodName)){
						return method.invoke(obj, args);
					}
				}
				throw new Exception("Method " + methodName + " not found");
			}
		} catch (Exception e){
			throw e;
		}
	}	
}
