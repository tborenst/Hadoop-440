/**
 * The ThreadProcess class takes in a MigratableProcess object and creates a new Thread object in which to run. It provides several public methods
 * to control the thread that's running the process. ThereadProcess keeps track of both the Thread and the Runnable it's running.
 */

package processManager;

import migratableProcesses.MigratableProcess;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import transactionaFileIO.tFile;
import util.Util;

public class ThreadProcess {
	private MigratableProcess process;
	private Thread thread;
	private int id;
	private String name;
	
	public ThreadProcess(String name, int id, String[] args) {
		Class<?> myClass = null;
		try {
			myClass = Class.forName(name);
		} catch (ClassNotFoundException e) {
			System.out.println("ThreadProcess.ThreadProcess: process does not exist: "+name);
			e.printStackTrace();
		}
		
		Constructor<?> myConstructor = null;
		
		try {
			myConstructor = myClass.getDeclaredConstructor(String[].class);
		} catch (NoSuchMethodException e) {
			System.out.println("ThreadProcess.ThreadProcess: constructor for process does not exist: "+name);
			e.printStackTrace();
		} catch (SecurityException e) {
			System.out.println("ThreadProcess.ThreadProcess: cannot get constructor of process due to security issue: "+name);
			e.printStackTrace();
		} 
		
		
		Object[] arguments = {args};
		try {
			//like .apply in javascript - takes in an array of arguments
			this.process = (MigratableProcess) myConstructor.newInstance(arguments);
		} catch (InstantiationException e) {
			System.out.println("ThreadProcess.ThreadProcess: unable to initialize process: "+name);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("ThreadProcess.ThreadProcess: unable to initialize process (illegal access): "+name);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("ThreadProcess.ThreadProcess: process does not exist (bad arguments): "+name+" args: "+Util.stringifyArray(args));
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			System.out.println("ThreadProcess.ThreadProcess: unable to initialize process: "+name);
			e.printStackTrace();
		}
		
		this.id = id;
		this.name = name;
		this.thread = new Thread(this.process);
	}
	
	public ThreadProcess(MigratableProcess process, int id, String name){
		this.process = process;
		this.id = id;
		this.name = name;
		this.thread = new Thread(process);
	}
	
	public ThreadProcess(String serPath, int id, String name, Boolean deleteAfterLoad) {
		this(ThreadProcess.deserialize(serPath, deleteAfterLoad), id, name);
	}
	
	/**
	 * void suspend(void):
	 * Calls the suspend() method on the process so that it enters a safe state.
	 */
	public void suspend(){
		process.suspend();
	}
	
	/**
	 * void start(void):
	 * Starts the thread object.
	 */
	public void start(){
		thread.start();
	}
	
	/**
	 * int getId(void):
	 * Return the process' id.
	 */
	public int getId(){
		return id;
	}
	
	/**
	 * String getName(void):
	 * Return the process' name.
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Boolean isAlive(void):
	 * Return the thread.isAlive().
	 * @return Boolean
	 */
	public Boolean isAlive() {
		Thread.State tState = thread.getState();
		System.out.print(tState.equals(Thread.State.TERMINATED));
		return !tState.equals(Thread.State.TERMINATED);
	}
	
	/**
	 * tFile serialize(String path):
	 * Forces the process into a safe state and then serializes it into a file saved to "path".
	 */
	public tFile serialize(String path){
		return serialize(new tFile(path, true));
	}
	
	/**
	 * tFile serialize(tFile serFile):
	 * Forces the process into a safe state and then serializes it into a the file.
	 * @param serFile
	 * @return tFile
	 */
	public tFile serialize(tFile serFile){
		process.suspend();
		if(serFile.exists()) {
			serFile.writeObj((Object) process);
			return serFile;
		}
		return null;
	}
	
	/**
	 * MigratableProcess deserialize(String path, Boolean deleteAfterLoad):
	 * Opens the file at path then recovers and returns the serialized process.
	 * Deletes the file if deleteAfterLoad is true.
	 * @param path
	 * @param deleteAfterLoad
	 * @return MigratableProcess
	 */
	public static MigratableProcess deserialize(String path, Boolean deleteAfterLoad) {
		tFile serFile = new tFile(path, false);
		MigratableProcess p = null;
		if(serFile.exists()) {
			p = (MigratableProcess) serFile.readObj();
			if(deleteAfterLoad) {
				serFile.delete();
			}
		}
		return p;
	}
	
}
