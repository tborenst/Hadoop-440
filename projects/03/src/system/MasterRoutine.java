/**
 * The MasterRoutine class will be called by the machine that is designated to be the master of system.
 */
package system;

import java.util.LinkedList;
import java.util.Queue;

import networking.SIOServer;

public class MasterRoutine {
	private SIOServer sio;            // socket
	private String workDirPath;       // working directory
	
	private LinkedList<Task> failedQueue;         // #1 priority (tasks that came back and failed)
	private LinkedList<Task> reduceQueue;         // #2 priority (reduce tasks)
	private LinkedList<Task> sortQueue;       // #3 priority (sorting tasks)
	private LinkedList<Task> mapQueue;            // #4 priority (map tasks)
	
	public MasterRoutine(int port, String workDirPath){
		this.sio = new SIOServer(port);
		this.workDirPath = workDirPath;
		this.failedQueue = new LinkedList<Task>();
		this.reduceQueue = new LinkedList<Task>();
		this.sortQueue = new LinkedList<Task>();
		this.mapQueue = new LinkedList<Task>();
	}
	
	//TODO: make this function work
	public void intepretConfigFile(){
		
	}
	
	
	
}
