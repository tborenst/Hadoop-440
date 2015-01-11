/**
 * The Task class is used to represent a sub-process of a larger map-reduce job, such as instructions for a particular mapper
 * or reducer.
 */

package system;

import java.io.Serializable;

public class Task implements Serializable{
	private static final long serialVersionUID = 5041639444045425129L;
	
	private int taskID;          // task id
	private int jobID;           // parent job's id
	private String type;         // map, sort, or reduce
	private String status;       // ready, pending, or completed
	
	private String classDir;     // path to directory containing .class file of mapper, reducer, etc...
	private String classFile;    // name of .class file of mapper, reducer, etc...
	private String className;    // binary name of .class file of mapper, reducer, etc...
	
	private String secondaryDir; // for example, the optional combiner method
	private String secondaryFile;
	private String secondaryName;
	
	// NOTE: map and reduce tasks should only have 1 'from' path and 1 'to' path.
	// On the other hand, sort tasks may have multiple 'from' and 'to' paths. 
	// The client should check that invariant when executing tasks.
	private String[] pathsFrom;  // path of initial records to map
	private String[] pathsTo;    // path to place results of map
	
	/**
	 * Task - represent a sub-process of a larger map-reduce job.
	 * @param taskID - this task's id, unique to parent job
	 * @param jobID - parent job's id 
	 * @param type - type ("map", "sort", or "reduce")
	 * @param classFile - path to .class file of mapper, reducer, etc...
	 * @param from - path of initial records to map
	 * @param to - path to place results of map
	 */
	public Task(int taskID, int jobID, String type, String[] classDir, String[] classFile, String[] className, String[] from, String[] to){
		this.taskID = taskID;
		this.jobID = jobID;
		this.type = type;
		this.status = Constants.READY; //initially, always ready
		
		this.classDir = classDir[0];
		this.classFile = classFile[0];
		this.className = className[0];
		
		this.secondaryDir = classDir[1];
		this.secondaryFile = classFile[1];
		this.secondaryName = className[1];
		
		this.pathsFrom = from;
		this.pathsTo = to;
	}
	
	public int getTaskID(){
		return taskID;
	}
	
	public int getJobID(){
		return jobID;
	}
	
	public String getTaskType(){
		return type;
	}
	
	public String getTaskStatus(){
		return status;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public String getClassDir(){
		return classDir;
	}
	
	public String getClassFile(){
		return classFile;
	}
	
	public String getClassName(){
		return className;
	}
	
	public String getSecondaryDir(){
		return secondaryDir;
	}
	
	public String getSecondaryFile(){
		return secondaryFile;
	}
	
	public String getSecondaryName(){
		return secondaryName;
	}
	
	public String[] getPathFrom(){
		return pathsFrom;
	}
	
	public String[] getPathTo(){
		return pathsTo;
	}

	
	// TODO: for debugging, remove at the end
	
	public void printTask(){
		System.out.println("====================");
		System.out.println("JOB ID: " + jobID + ", TASK ID: " + taskID);
		System.out.println("TYPE: " + type);
		System.out.println("STATUS: " + status);
		System.out.println();
		System.out.println("=> FROM: ");
		for(int i = 0; i < pathsFrom.length; i++){
			System.out.println("=> " + pathsFrom[i]);
		}
		System.out.println("=> TO: ");
		for(int i = 0; i < pathsTo.length; i++){
			System.out.println("=> " + pathsTo[i]);
		}
		System.out.println("====================");
	}
}
