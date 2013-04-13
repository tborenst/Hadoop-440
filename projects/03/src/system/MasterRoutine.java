/**
 * The MasterRoutine class will be called by the machine that is designated to be the master of system.
 * @author Tomer Borenstein
 */
package system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import networking.SIOCommand;
import networking.SIOServer;
import networking.SIOSocket;

public class MasterRoutine {
	private SIOServer sio;                    // socket
	private String workDirPath;               // working directory
	
	private LinkedList<Task> failedQueue;     // #1 priority (tasks that came back and failed)
	private LinkedList<Task> reduceQueue;     // #2 priority (reduce tasks)
	private LinkedList<Task> sortQueue;       // #3 priority (sorting tasks)
	private LinkedList<Task> mapQueue;        // #4 priority (map tasks)
	
	int jobCount;                             // how many jobs have been started, also used as job ids
	private HashMap<Integer, Job> jobs;       // mapping job id's to job object
	
	private HashMap<SIOSocket, Task> pendingSockets; // mapping slave sockets to tasks
	private LinkedList<SIOSocket> idleSockets;       // idle slave sockets available for work
	
	public MasterRoutine(int port, String workDirPath){
		this.sio = new SIOServer(port);
		this.workDirPath = workDirPath;
		this.failedQueue = new LinkedList<Task>();
		this.reduceQueue = new LinkedList<Task>();
		this.sortQueue = new LinkedList<Task>();
		this.mapQueue = new LinkedList<Task>();
		this.jobCount = 0;
		this.jobs = new HashMap<Integer, Job>();
		this.pendingSockets = new HashMap<SIOSocket, Task>();
		this.idleSockets = new LinkedList<SIOSocket>();
		handleSockets();
	}
	
	/**
	 * handleSockets - all socket manipulation goes in here
	 */
	private void handleSockets(){
		// TODO: receive request object and interpret it
		
		// add socket to idle pool when first connected
		sio.on("connection", new SIOCommand(){
			public void run(){
				synchronized(idleSockets){
					idleSockets.add(socket);
				}
				
				// new slaves
				idleSockets.add(socket);
			}
		});
		
		sio.on(Constants.TASK_COMPLETE, new SIOCommand(){
			public void run(){
				Task task = (Task)object;
				String type = task.getTaskType();
				
				int jobID = task.getJobID();
				int taskID = task.getTaskID();
				
				// update task status on the server
				synchronized(jobs){
					Job job = jobs.get(jobID);
					if(type.equals(Constants.MAP)){
						boolean done = job.updateMapTask(taskID, Constants.COMPLETED);
						if(done){
							//start sort phase
							Task sortTask = job.generateSortTask();
							synchronized(sortQueue){
								sortQueue.add(sortTask);
								job.updateSortTask(sortTask.getTaskID(), Constants.PENDING);
							}
						}
					} else if(type.equals(Constants.SORT)){
						boolean done = job.updateSortTask(taskID, Constants.COMPLETED);
						if(done){
							//start reduce phase
							HashMap<Integer, Task> reduceTasks = job.generateReduceTasks();
							Iterator<Entry<Integer, Task>> it = reduceTasks.entrySet().iterator();
							while(it.hasNext()){
								// get reduce task
								Entry<Integer, Task> entry = it.next();
								int id = entry.getKey();
								Task t = entry.getValue();
								// add it to reduce task queue
								reduceQueue.add(t);
								// update task status
								job.updateReduceTask(id, Constants.PENDING);
							}
						}
					} else if(type.equals(Constants.REDUCE)){
						// TODO: figure out what to do here
						System.out.println("Job " + jobID + "is done.");
					} else{
						try {
							throw new Throwable("Unrecognized task type: " + type);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
				
				// release socket into idle queue
				synchronized(pendingSockets){
					pendingSockets.remove(socket);
				}
				synchronized(idleSockets){
					idleSockets.add(socket);
				}
				
				// there are now idle slaves and perhaps new tasks to be completed
				spendResources();
			}
		});
		
		// get socket's task and requeue it if disconnected while working
		sio.on("disconnect", new SIOCommand(){
			public void run(){
				synchronized(pendingSockets){
					// get failed task, delete socket
					Task failedTask = pendingSockets.get(socket);
					pendingSockets.remove(socket);
					// requeue failed task
					synchronized(failedQueue){
						if(failedTask != null){
							failedQueue.add(failedTask);
						}
					}
				}
			}
		});
	}
	
	/**
	 * spendResources - if there are any slaves available, start assigning them tasks with the following priorities:
	 * 1. failed tasks
	 * 2. reduce tasks
	 * 3. sort tasks
	 * 4. map tasks
	 */
	private void spendResources(){
		synchronized(idleSockets){
			synchronized(failedQueue){
				// #1 priority
				while(!failedQueue.isEmpty() && !idleSockets.isEmpty()){
					SIOSocket slave = idleSockets.remove();
					Task failedTask = failedQueue.remove();
					slave.emit(Constants.TASK_REQUEST, failedTask);
					synchronized(pendingSockets){
						pendingSockets.put(slave, failedTask);
					}
				}
				
				synchronized(reduceQueue){
					// #2 priority
					while(!reduceQueue.isEmpty() && !idleSockets.isEmpty()){
						SIOSocket slave = idleSockets.remove();
						Task reduceTask = reduceQueue.remove();
						slave.emit(Constants.TASK_REQUEST, reduceTask);
						synchronized(pendingSockets){
							pendingSockets.put(slave, reduceTask);
						}
					}
				
					synchronized(sortQueue){
						// #3 priority
						while(!sortQueue.isEmpty() && !idleSockets.isEmpty()){
							SIOSocket slave = idleSockets.remove();
							Task sortTask = sortQueue.remove();
							slave.emit(Constants.TASK_REQUEST, sortTask);
							synchronized(pendingSockets){
								pendingSockets.put(slave, sortTask);
							}
						}
						
						synchronized(mapQueue){
							// #4 priority
							while(!mapQueue.isEmpty() && !idleSockets.isEmpty()){
								SIOSocket slave = idleSockets.remove();
								Task mapTask = mapQueue.remove();
								slave.emit(Constants.TASK_REQUEST, mapTask);
								synchronized(pendingSockets){
									pendingSockets.put(slave, mapTask);
								}
							}
						}
						
					}
				}
				
			}
		}
	}
	
	/**
	 * createJob - add a new job to be processed, generate its initial map tasks, and add them to the task queue
	 * @param mappers - how many mappers to work on this job
	 * @param reducers - how many reducers to work on this job
	 * @param mapperDir - directory of mapper class
	 * @param mapperFile - file name of mapper class
	 * @param mapperName - binary name of mapper class
	 * @param reducerDir - directory of reducer class
	 * @param reducerFile - file name of reducer class
	 * @param reducerName - binary name of reducer class
	 * @param combinerDir - directory of combiner class
	 * @param combinerFile - file name of combiner class
	 * @param combinerName - binary name of combiner class
	 * @param fromPaths - paths to already-partitioned data for this map-reduce process
	 * @param resultsDir - directory to put the result of this map-reduce process
	 */
	public void createJob(int mappers, int reducers,
						  String mapperDir, String mapperFile, String mapperName,
						  String reducerDir, String reducerFile, String reducerName,
						  String combinerDir, String combinerFile, String combinerName,
						  String[] fromPaths, String resultsDir){
		// create job
		Job job = new Job(jobCount, mappers, reducers, workDirPath, fromPaths, resultsDir);
		job.setMapper(mapperDir, mapperFile, mapperName);
		job.setReducer(reducerDir, reducerFile, reducerName);
		
		// generate map tasks and add them to queue
		synchronized(mapQueue){
			HashMap<Integer, Task> mapTasks = job.generateMapTasks();
			
			Iterator<Entry<Integer, Task>> it = mapTasks.entrySet().iterator();
			while(it.hasNext()){
				// get map task
				Entry<Integer, Task> entry = it.next();
				int id = entry.getKey();
				Task t = entry.getValue();
				// add it to map task qeue
				mapQueue.add(t);
				// update task status
				job.updateMapTask(id, Constants.PENDING);
			}
			
			// add job to table
			synchronized(jobs){
				jobs.put(jobCount, job);
				jobCount++;
			}
			
		}
		
		// new job
		spendResources();
	}
	
	
	
}