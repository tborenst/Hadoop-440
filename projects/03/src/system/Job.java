/**
 * The Job class is used to represent a whole map-reduce process.
 * @author Tomer Borenstein
 */

package system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Job {
	
	// IMPORTANT DATA //
	private int jobID;                          // job id
	private int taskCount;                      // how many tasks for this job, also used as task ids
	
	private int mappers;                        // how many mappers will be working on this job
	private int reducers;                       // how many reducers will be working on this job
	
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
	 * @throws error - if the number of mappers does not equal the number of input files (String[] from)
	 */
	public Job(int id, int mappers, int reducers, String workDir, String[] from, String to) throws Throwable{
		// check that number of mappers is the same as the number of "from" paths
		if(mappers != from.length){
			throw new Throwable("Number of mappers is not the same as number of input data files for job [id #" + jobID + "]");
		}
		this.jobID = id;
		this.taskCount = 0;
		
		this.mappers = mappers;
		this.reducers = reducers;
		
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
	 * @throws error - if mapper not configured before using
	 */
	public HashMap<Integer, Task> generateMapTasks() throws Throwable{
		// check that a mapper has been set, throw an error if not
		if(mapperDir == null || mapperFile == null || mapperName == null){
			throw new Throwable("Mapper not configured for job [id #" + jobID + "]");
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
	public HashMap<Integer, Task> generateReduceTasks() throws Throwable{
		// check that a reducer has been set, throw an error if not
		if(reducerDir == null || reducerFile == null || reducerName == null){
			throw new Throwable("Reducer not configured for job [id #" + jobID + "]");
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
