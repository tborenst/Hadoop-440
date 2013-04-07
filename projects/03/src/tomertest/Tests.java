package tomertest;

import java.io.IOException;

import util.Executer;

public class Tests {
	public static void main(String[] args) throws IOException{
		//=============================
		// Dynamically Load Class Files
		// - initiate objects
		// - invoke methods
		//=============================
		String path = "/Users/tomer/Desktop/Box/school/15440/projects/03/bin/tomertest";
		Executer executer = new Executer();
		Class <?> testClass = executer.getClass(path, "TestObject.class", "tomertest.TestObject");
		Object[] constructorArgs = {1};
		Object testObject = executer.instantaite(testClass, constructorArgs);
		Object[] methodArgs = {4};
		System.out.println("===============");
		System.out.println("Method - sayNI:");
		executer.execute(testObject, "sayNI", methodArgs);
		System.out.println("===============");
	}
}
