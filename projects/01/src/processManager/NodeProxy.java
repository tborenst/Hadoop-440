/**
 * The NodeProxy class is a representation of slave nodes on the master node. 
 * Make sure that you synchronize(node) on any node you intend to interact with.
 */

package processManager;

import java.util.ArrayList;
import java.util.Iterator;

public class NodeProxy {
	private int id; //corresponds to its socket id
	private ArrayList<ProcessProxy> processes;
	
	public NodeProxy(int id){
		this.id = id;
		this.processes = new ArrayList<ProcessProxy>();
	}
	
	public void addProcess(int id, String name){
		ProcessProxy process = new ProcessProxy(id, name);
		processes.add(process);
	}
	
	public int getId(){
		return id;
	}
	
	public ProcessProxy setFinished(int id){
		Iterator<ProcessProxy> iterator = processes.iterator();
		while(iterator.hasNext()){
			ProcessProxy process = iterator.next();
			if(process.getId() == id){
				process.setFinished();
				return process;
			}
		}
		return null;
	}
	
	/**
	 * ProcessProxy removeOldestProcess(void):
	 * Removes oldest process from processes list and returns it.
	 * @return - oldest process.
	 */
	public ProcessProxy removeOldestProcess(){
		cleanUp();
		return processes.remove(0);
	}
	
	/**
	 * ProcessProxy removeRandomProcess(void):
	 * Removes a random (not dead) process from processes list and returns it.
	 * @return - random process.
	 */
	public ProcessProxy removeRandomProcess(){
		cleanUp();
		int index = (int)(Math.random() * ((processes.size() - 0) + 1));
		return processes.remove(index);
	}
	
	public ProcessProxy removeProcessById(int id){
		ProcessProxy process = setFinished(id);
		cleanUp();
		return process;
	}
	
	/**
	 * void cleanUp(void):
	 * Removes any finished processes from the list.
	 */
	public void cleanUp(){
			ArrayList<ProcessProxy> cleanList = new ArrayList<ProcessProxy>();
			Iterator<ProcessProxy> iterator = processes.iterator();
			while(iterator.hasNext()){
				ProcessProxy process = iterator.next();
				if(!process.isFinished()){
					//still not finished
					cleanList.add(process);
				}
			}
			processes = cleanList;
	}
	
	
	/**
	 * ProcessProxy class for convenience.
	 * Takes in (int id, String name), and has a getId(), agetName(), a setFinished() and isFinished() methods.
	 */
	public class ProcessProxy{
		private int id;
		private String name;
		private Boolean finished;
		
		public ProcessProxy(int id, String name){
			this.id = id;
			this.name = name;
			this.finished = false;
		}
		
		public int getId(){
			return id;
		}
		
		public String getName(){
			return name;
		}
		
		public void setFinished(){
			synchronized(finished){
				finished = true;
			}
		}
		
		public Boolean isFinished(){
			synchronized(finished){
				return finished;
			}
		}
		
	}
}
