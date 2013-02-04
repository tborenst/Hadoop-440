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
	
	/**
	 * String getProcessesAsString(void):
	 * Returns a String that describes all the processes running on this node.
	 * @return String
	 */
	public String getProcessesAsString(){
		String string = "----------------------------------------------------\n"
				      + "Node ID: " + id + "\n"
				      + "----------------------------------------------------\n";
		synchronized(processes){
			Iterator<ProcessProxy> iterator = processes.iterator();
			while(iterator.hasNext()){
				ProcessProxy process = iterator.next();
				String temp = "Process: " + process.getId() + " | " + process.getName() + "\n";
				string += temp;
			}
			string += "----------------------------------------------------\n";
		}
		return string;
	}
	
	/**
	 * int getNumberOfProcesses(void):
	 * Returns the number of processes running on this node.
	 * @return
	 */
	public int getNumberOfProcesses(){
		synchronized(processes){
			return processes.size();
		}
	}
	
	public ProcessProxy getProcessById(int processId){
		synchronized(processes){
			Iterator<ProcessProxy> iterator = processes.iterator();
			while(iterator.hasNext()){
				ProcessProxy process = iterator.next();
				if(process.getId() == processId){
					//process found
					return process;
				}
			}
			//process not found
			return null;
		}
	}
	
	public void addNewProcess(int id, String name){
		synchronized(processes){
			ProcessProxy process = new ProcessProxy(id, name);
			processes.add(process);
		}
	}
	
	public void addExistingProcess(ProcessProxy process){
		synchronized(processes){
			processes.add(process);
		}
	}
	
	public ProcessProxy setFinished(int id){
		synchronized(processes){
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
	}
	
	public int getId(){
		return id;
	}
	
	/**
	 * ProcessProxy getRandomProcess(void):
	 * Returns a random (not dead) process from processes list, but does not remove it.
	 * @return - random process.
	 */
	public ProcessProxy getRandomProcess(){
		synchronized(processes){
			cleanUp();
			int index = (int)(Math.random() * ((processes.size() - 0) + 1));
			return processes.get(index);
		}
	}
	
	/**
	 * ProcessProxy removeProcessById(int):
	 * Removes a process with a certain id from the list and returns it.
	 * @param id
	 * @return - removed process.
	 */
	public ProcessProxy removeProcessById(int id){
		synchronized(processes){
			ProcessProxy process = setFinished(id);
			cleanUp();
			return process;
		}
		
	}
	
	/**
	 * void cleanUp(void):
	 * Removes any finished processes from the list.
	 */
	public void cleanUp(){
		synchronized(processes){
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
