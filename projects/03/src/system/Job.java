/**
 * The Job class is used to represent a whole map-reduce process.
 * @author Tomer Borenstein
 */

package system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Job {
	
	// IMPORTANT DATA //
	private String jobStatus;                   // job status - map phase, 
	private int jobID;                          // job id
	private int taskCount;                      // how many tasks for this job, also used as task ids
	
	private int mappers;                        // how many mappers will be working on this job
	private int reducers;                       // how many reducers will be working on this job
	
	private int mapTasksDone;                   // keeps count of how many map tasks are complete
	private int sortTasksDone;                  // keeps count of how many sort tasks are complete (should be 0 or 1)
	private int reduceTasksDone;                // keeps count of how many reduce tasks are complete
	
	// TASK MANIPULATION //
	private HashMap<Integer, Task> mapTasks;    // map tasks (id, task)
	private Task sortTask;                      // sort task* (there should be only one)
	private HashMap<Integer, Task> reduceTasks; // reduce tasks (id, task)
	
	private String workDir;                     // path to the system's working directory
	private String[] from;                      // path(s) to initial, unprocessed data
	private String to;                          // path to *directory* of results
	
	private String mapperDir;                   // directory containing mapper
	private String mapperFile;                  // name of .java mapper file
	private String mapperName;                  // binary name of mapper file
	
	private String reducerDir;                  // directory containing reducer
	private String reducerFile;                 // name of .java reducer file
	private String reducerName;                 // binary name of reducer file
	
	// INTERNAL USE //
	private String[] interMapFiles;             // paths of intermideary files coming out of mappers
	private String[] interSortFiles;            // paths of intermideary files coming out of the sort & shuffle phase
	
	/**
	 * Job - represents an entire map-reduce operation
	 * @param id - unique job id
	 * @param mappers - number of mappers to be working on the job
	 * @param reducers - number of reduces to be working on the job
	 * @param workDir - system's working directory
	 * @param from - array of paths to input files (should already be partitioned by the master)
	 * @param to - array of directory to put results in (there will be as many result files as there are reducers)
	 */
	public Job(int id, int mappers, int reducers, String workDir, String[] from, String to){
		// check that number of mappers is the same as the number of "from" paths
		if(mappers != from.length){
			try{
				throw new Throwable("Number of mappers is not the same as number of input data files for job [id #" + jobID + "]");
			} catch (Throwable e){
				e.printStackTrace();
			}
		}
		
		this.jobStatus = Constants.MAPPING;
		this.jobID = id;
		this.taskCount = 0;
		
		this.mappers = mappers;
		this.reducers = reducers;
		
		this.mapTasksDone = 0;
		this.sortTasksDone = 0;
		this.reduceTasksDone = 0;
		
		this.mapTasks = new HashMap<Integer, Task>();
		this.mapTasks = new HashMap<Integer, Task>();
		this.reduceTasks = new HashMap<Integer, Task>();
		
		this.workDir = workDir;
		this.from = from;
		this.to = to;
		
		this.interMapFiles = new String[mappers];
		this.interSortFiles = new String[reducers];
	}
	
	/**
	 * setMapper - tell the job which Mapper class to use
	 * @param mapperDir - directory of Mapper .class file
	 * @param mapperFile - name of Mapper .class file
	 * @param mapperName - binary name of Mapper .class file
	 */
	public void setMapper(String mapperDir, String mapperFile, String mapperName){
		this.mapperDir = mapperDir;
		this.mapperFile = mapperFile;
		this.mapperName = mapperName;
	}
	
	/**
	 * setReducer - tell the job which Reducer class to use
	 * @param reducerDir - the directory of Reducer .class file
	 * @param reducerFile - name of Reducer .class file
	 * @param reducerName - binary name of Reducer .class file
	 */
	public void setReducer(String reducerDir, String reducerFile, String reducerName){
		this.reducerDir = reducerDir;
		this.reducerFile = reducerFile;
		this.reducerName = reducerName;
	}
	
	/**
	 * generateMapTasks - add all requried map tasks for this job, to this job, and return them
	 * @return tasks - HashMap<Integer, Task>, mapping task ids to tasks
	 */
	public HashMap<Integer, Task> generateMapTasks(){
		// check that a mapper has been set, throw an error if not
		try{
			if(mapperDir == null || mapperFile == null || mapperName == null){
				throw new Throwable("Mapper not configured for job [id #" + jobID + "]");
			}
		} catch (Throwable e){
			e.printStackTrace();
		}
		
		// generate map tasks
		for(int i = 0; i < mappers; i++){
			String[] fromPath = {from[i]};                                                         // input file for mapper
			String[] interPath = {workDir + "/job" + jobID + "maptask" + taskCount + "mapresult"}; // intermediary file from mapper
			Task task = new Task(taskCount, jobID, Constants.MAP, mapperDir, mapperFile, mapperName, fromPath, interPath);
			
			mapTasks.put(taskCount, task);    // add task to job
			interMapFiles[i] = interPath[0];  // track intermideary file
			taskCount++;
		}
		
		return mapTasks;
	}

	/**
	 * generateSortTask - adds one sorting task for this job, to this job, and returns it.
	 * @return task - Task (sorting task)
	 */
	public Task generateSortTask(){
		String[] fromPaths = interMapFiles;
		String[] toPaths = new String[reducers];
		for(int i = 0; i < reducers; i++){
			toPaths[i] = workDir + "/job" + jobID + "sorttask" + i + "sortresult";
			interSortFiles[i] = toPaths[i];
		}
		Task task = new Task(taskCount, jobID, Constants.SORT, null, null, null, fromPaths, toPaths);
		sortTask = task;
		taskCount++;
		return task;
	}
	
	/**
	 * generateReduceTasks - add all required reduce tasks for this job, to this job, and return them
	 * @return tasks - HashMap<Integer, Task>, mapping task ids to tasks
	 * @throws error - if mapper not configured before using
	 */
	public HashMap<Integer, Task> generateReduceTasks(){
		// check that a reducer has been set, throw an error if not
		try{
			if(reducerDir == null || reducerFile == null || reducerName == null){
				throw new Throwable("Reducer not configured for job [id #" + jobID + "]");
			}
		} catch (Throwable e){
			e.printStackTrace();
		}
		
		// generate reduce tasks
		for(int i = 0; i < reducers; i++){
			String[] fromPath = {interSortFiles[i]};
			String[] finalPath = {to + "/job" + jobID + "reducetask" + taskCount + "finalresult"};
			Task task = new Task(taskCount, jobID, Constants.REDUCE, reducerDir, reducerFile, reducerName, fromPath, finalPath);
			
			reduceTasks.put(taskCount, task);
			taskCount++;
		}
		
		return reduceTasks;
	}
	
	/**
	 * updateMapTask - set the map task represented by id to a certain status. If all map tasks are
	 * complete, this method will return true (so you know you need to sort next, etc...), otherwise it will
	 * return false.
	 * @param id - map task's id
	 * @param status - new task status
	 * @return boolean - true if all map tasks are complete, false otherwise
	 */
	public boolean updateMapTask(int id, String status){
		Task task = mapTasks.get(id); // get map task
		
		if(task == null){
			return false;
		}
		
		task.setStatus(status); // update task status
		mapTasks.put(id, task); // put map task back into table
		
		if(status.equals(Constants.COMPLETED)){
			mapTasksDone++;
		}
		
		
		// make sure everything makes sense
		if(mapTasksDone > mappers){
			try {
				throw new Throwable("More map tasks done than mappers for job [id #" + jobID + "]");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		if(mapTasksDone == mappers){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * updateSortTask - set the sort task represented by id to a certain status. If all (1 of) sort tasks are
	 * complete, this method will return true (so you know to reduce next, etc...), otherwise it will return false.
	 * @param id - sort task's id
	 * @param status - new task status
	 * @return boolean - true if all sort tasks are complete, false otherwise
	 */
	public boolean updateSortTask(int id, String status){
		Task task = sortTask;
		
		// make sure everything makes sense
		if(task.getTaskID() != id){
			try {
				throw new Throwable("Task ID does not match sort task ID for job [id #" + jobID + "]");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		sortTask.setStatus(status);
		
		if(status.equals(Constants.COMPLETED)){
			sortTasksDone++;
		}
		
		// make sure everything makes sense
		if(sortTasksDone > 1){
			try {
				throw new Throwable("More sort tasks done than 1 for job [id #" + jobID + "]");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		if(sortTasksDone == 1){
			return true;
		} else {
			return false;
		}
		
		
	}
	
	/**
	 * updateReduceTask - set the redice task represented by id to a certain status. If all reduce tasks are
	 * complete, this method will return true (so you know you're done, etc...), otherwise it will
	 * return false.
	 * @param id - reduce task's id
	 * @param status - new task status
	 * @return boolean - true if all reduce tasks are complete, false otherwise
	 */
	public boolean updateReduceTask(int id, String status){
		Task task = reduceTasks.get(id); // get reduce task
		
		if(task == null){
			return false;
		}
		
		task.setStatus(status); // update task status
		reduceTasks.put(id, task); // put reduce task back into table
		
		if(status.equals(Constants.COMPLETED)){
			reduceTasksDone++;
		}
		
		
		// make sure everything makes sense
		if(reduceTasksDone > reducers){
			try {
				throw new Throwable("More reduce tasks done than reducers for job [id #" + jobID + "]");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		if(reduceTasksDone == reducers){
			return true;
		} else {
			return false;
		}
	}
	
	public void updateJobStatus(String status){
		jobStatus = status;
	}
	
	public String getJobStatus(){
		return jobStatus;
	}
	
	public int getJobID(){
		return jobID;
	}
	
	// TODO: for debugging, remove at the end
	
	public void printMapTasks(){
		Iterator<Entry<Integer, Task>> it = mapTasks.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Task> entry = it.next();
			entry.getValue().printTask();
		}
	}
	
	public void printSortTask(){
		sortTask.printTask();
	}
	
	public void printRedcueTasks(){
		Iterator<Entry<Integer, Task>> it = reduceTasks.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Task> entry = it.next();
			entry.getValue().printTask();
		}
	}
	
}
