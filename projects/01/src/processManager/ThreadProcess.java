/**
 * The ThreadProcess class takes in a MigratableProcess object and creates a new Thread object in which to run. It provides several public methods
 * to control the thread that's running the process. ThereadProcess keeps track of both the Thread and the Runnable it's running.
 */

package processManager;

import migratableProcesses.MigratableProcess;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import transactionaFileIO.tFile;

public class ThreadProcess {
	private MigratableProcess process;
	private Thread thread;
	private int id;
	
	public ThreadProcess(MigratableProcess process, int id){
		this.process = process;
		this.id = id;
		this.thread = new Thread(process);
	}
	
	/**
	 * void suspend(void):
	 * Calls the suspend() method on the process so that it enters a safe state.
	 */
	private void suspend(){
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
	 * void serialize(String path):
	 * Forces the process into a safe state and then serializes it into a file saved to "path".
	 */
	//TODO: change this to use TransactionalFileIO
	public void serialize(String path){
		process.suspend();
		
		tFile serFile = new tFile(path);
		serFile.writeObj((Object) process);
		
		/*try{
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(process);
			objOut.close();
		} catch (IOException e){
			System.out.println("Could not serialize the following process:");
			System.out.println(process.toString());
		}*/
	}
	
}
