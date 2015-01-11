/**
 * The MasterRoutine class will be called by the machine that is designated to be the master of system.
 * @author Tomer Borenstein
 */
package system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import api.JobStatus;

import fileio.RecordsFileIO;

import networking.SIOCommand;
import networking.SIOServer;
import networking.SIOSocket;

public class MasterRoutine {
	private SIOServer slaveSIO;               // socket for slaves
	private SIOServer clientSIO;          	  // socket for clients
	private String workDirPath;               // working directory
	
	private LinkedList<Task> failedQueue;     // #1 priority (tasks that came back and failed)
	private LinkedList<Task> reduceQueue;     // #2 priority (reduce tasks)
	private LinkedList<Task> sortQueue;       // #3 priority (sorting tasks)
	private LinkedList<Task> mapQueue;        // #4 priority (map tasks)
	
	int jobCount;                             // how many jobs have been started, also used as job ids
	Integer fileCount;                        // how many files we've used as temp files for partitioning
	private HashMap<Integer, Job> jobs;       // mapping job id's to job object
	
	private HashMap<SIOSocket, Task> pendingSockets; // mapping slave sockets to tasks
	private LinkedList<SIOSocket> idleSockets;       // idle slave sockets available for work
	private HashMap<Integer, SIOSocket> clientJobs;  // mapping job ids to their respective client socket
	
	public MasterRoutine(int slavePort, int clientPort, String workDirPath){
		this.slaveSIO = new SIOServer(slavePort);
		this.clientSIO = new SIOServer(clientPort);
		this.workDirPath = workDirPath;
		this.failedQueue = new LinkedList<Task>();
		this.reduceQueue = new LinkedList<Task>();
		this.sortQueue = new LinkedList<Task>();
		this.mapQueue = new LinkedList<Task>();
		this.jobCount = 0;
		this.fileCount = 0;
		this.jobs = new HashMap<Integer, Job>();
		this.pendingSockets = new HashMap<SIOSocket, Task>();
		this.idleSockets = new LinkedList<SIOSocket>();
		this.clientJobs = new HashMap<Integer, SIOSocket>();
		handleSockets();
	}
	
	/**
	 * handleSockets - all socket manipulation goes in here
	 */
	private void handleSockets(){
		// CLIENT CONNECTIONS
		clientSIO.on(Constants.JOB_REQUEST, new SIOCommand(){
			@Override
			public void run(){
				Request req = (Request)object;
				int mappers = req.getNumMappers();
				int reducers = req.getNumReducers();
				String mapperDir = req.getMapperDirectory();
				String mapperFile = req.getMapperFileName();
				String mapperName = req.getMapperBinaryName();
				String reducerDir = req.getReducerDirectory();
				String reducerFile = req.getReducerFileName();
				String reducerName = req.getReducerBinaryName();
				String combinerDir = req.getCombinerDirectory();
				String combinerFile = req.getCombinerFileName();
				String combinerName = req.getCombinerBinaryName();
				String[] from = req.getDataPaths();
				String resultsDir = req.getResultsDirectory();
				
				// partition data
				String[] initialMapFiles = new String[mappers];
				synchronized(fileCount){
					for(int i = 0; i < initialMapFiles.length; i++){
						initialMapFiles[i] = workDirPath + "/partitiontempfile" + fileCount;
						fileCount++;
					}
				}
				
				try{
					RecordsFileIO.dealStringsAsRecordsTo(from, initialMapFiles, "\n", "\n");
				} catch (Exception e){
					socket.emit(Constants.JOB_REQUEST, new JobStatus(-1, Constants.FAILED));
				}
				
				// create new job
				int jobID = createJob(mappers, reducers, 
						  			  mapperDir, mapperFile, mapperName, 
						  			  reducerDir, reducerFile, reducerName,
						  			  combinerDir, combinerFile, combinerName,
						  			  initialMapFiles, resultsDir);
				
				synchronized(clientJobs){
					clientJobs.put(jobID, socket);
				}
				
				socket.emit(Constants.JOB_REQUEST, new JobStatus(jobID, Constants.MAPPING));
			}
		});
		
		// report job status
		clientSIO.on(Constants.JOB_STATUS, new SIOCommand(){
			public void run(){
				synchronized(jobs){
					int jobID = (Integer)object;
					Job job = jobs.get(jobID);
					if(job == null){
						socket.emit(Constants.JOB_STATUS, new JobStatus(jobID, Constants.NO_SUCH_JOB));
					} else {
						socket.emit(Constants.JOB_STATUS, new JobStatus(jobID, job.getJobStatus()));
					}
				}
			}
		});
		
		// stop new job if not failed or completed
		clientSIO.on(Constants.STOP_JOB, new SIOCommand(){
			public void run(){
				synchronized(jobs){
					int jobID = (Integer)object;
					Job job = jobs.get(jobID);
					if(job == null){
						socket.emit(Constants.JOB_STATUS, new JobStatus(jobID, Constants.NO_SUCH_JOB));
					} else {
						if(!(job.getJobStatus().equals(Constants.FAILED) || job.getJobStatus().equals(Constants.COMPLETED))){
							job.updateJobStatus(Constants.STOPPED);
						}
						socket.emit(Constants.JOB_STATUS, new JobStatus(jobID, job.getJobStatus()));
					}
				}
			}
		});
		
		// resume new job if not failed or completed
		clientSIO.on(Constants.START_JOB, new SIOCommand(){
			public void run(){
				synchronized(jobs){
					int jobID = (Integer)object;
					Job job = jobs.get(jobID);
					if(job == null){
						socket.emit(Constants.JOB_STATUS, new JobStatus(jobID, Constants.NO_SUCH_JOB));
					} else {
						if(!(job.getJobStatus().equals(Constants.FAILED) || job.getJobStatus().equals(Constants.COMPLETED))){
							job.updateJobStatus(Constants.PENDING);
						}
						socket.emit(Constants.JOB_STATUS, new JobStatus(jobID, job.getJobStatus()));
					}
				}
			}
		});
		
		
		// SLAVE CONNECTIONS
		
		// add socket to idle pool when first connected
		slaveSIO.on("connection", new SIOCommand(){
			public void run(){
				synchronized(idleSockets){
					idleSockets.add(socket);
				}
				
				// new slaves
				idleSockets.add(socket);
			}
		});
		
		// send errors back to the client
		slaveSIO.on(Constants.TASK_ERROR, new SIOCommand(){
			public void run(){
				Task task = (Task)object;
				int jobID = task.getJobID();
				synchronized(jobs){
					Job job = jobs.get(jobID);
					if(!job.getJobStatus().equals(Constants.FAILED)){
						job.updateJobStatus(Constants.FAILED);
						synchronized(clientJobs){
							SIOSocket client = clientJobs.get(jobID);
							client.emit(Constants.JOB_STATUS, new JobStatus(jobID, Constants.FAILED));
						}
					}
				}
			}
		});
		
		slaveSIO.on(Constants.TASK_COMPLETE, new SIOCommand(){
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
							job.updateJobStatus(Constants.SORTING);
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
							job.updateJobStatus(Constants.REDUCING);
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
						boolean done = job.updateReduceTask(taskID, Constants.COMPLETED);
						if(done){
							// let the client know
							SIOSocket client = clientJobs.get(job.getJobID());
							job.updateJobStatus(Constants.COMPLETED);
							client.emit(Constants.JOB_COMPLETE, new JobStatus(job.getJobID(), Constants.COMPLETED));
						}
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
		slaveSIO.on("disconnect", new SIOCommand(){
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
					synchronized(jobs){
						Job job = jobs.get(failedTask.getJobID());
						if(job.getJobStatus().equals(Constants.FAILED)){
							// discard of task, put socket back into idleSockets
							idleSockets.add(slave);
						} else if(job.getJobStatus().equals(Constants.STOPPED)){
							// requeue task for a later time, put socket back into idleSockets
							idleSockets.add(slave);
							failedQueue.add(failedTask);
						} else {
							// OK to send task
							slave.emit(Constants.TASK_REQUEST, failedTask);
							synchronized(pendingSockets){
								pendingSockets.put(slave, failedTask);
							}
						}
					}
				}
				
				synchronized(reduceQueue){
					// #2 priority
					while(!reduceQueue.isEmpty() && !idleSockets.isEmpty()){
						SIOSocket slave = idleSockets.remove();
						Task reduceTask = reduceQueue.remove();
						synchronized(jobs){
							Job job = jobs.get(reduceTask.getJobID());
							if(job.getJobStatus().equals(Constants.FAILED)){
								// discard of task, put socket back into idleSockets
								idleSockets.add(slave);
							} else if(job.getJobStatus().equals(Constants.STOPPED)){
								// requeue task for a later time, put socket back into idlesockets
								idleSockets.add(slave);
								reduceQueue.add(reduceTask);
							} else {
								// OK to send task
								slave.emit(Constants.TASK_REQUEST, reduceTask);
								synchronized(pendingSockets){
									pendingSockets.put(slave, reduceTask);
								}
							}
						}
					}
				
					synchronized(sortQueue){
						// #3 priority
						while(!sortQueue.isEmpty() && !idleSockets.isEmpty()){
							SIOSocket slave = idleSockets.remove();
							Task sortTask = sortQueue.remove();
							synchronized(jobs){
								Job job = jobs.get(sortTask.getJobID());
								if(job.getJobStatus().equals(Constants.FAILED)){
									// discard of task, put socket back into idleSockets
									idleSockets.add(slave);
								} else if(job.getJobStatus().equals(Constants.STOPPED)){
									// requeue task for a later time, put socket back into idleSockets
									idleSockets.add(slave);
									sortQueue.add(sortTask);
								} else {
									// OK to send task
									slave.emit(Constants.TASK_REQUEST, sortTask);
									synchronized(pendingSockets){
										pendingSockets.put(slave, sortTask);
									}
								}
							}
						}
						
						synchronized(mapQueue){
							// #4 priority
							while(!mapQueue.isEmpty() && !idleSockets.isEmpty()){
								SIOSocket slave = idleSockets.remove();
								Task mapTask = mapQueue.remove();
								synchronized(jobs){
									Job job = jobs.get(mapTask.getJobID());
									if(job.getJobStatus().equals(Constants.FAILED)){
										// discard of task, put socket back into idlesockets
										idleSockets.add(slave);
									} else if(job.getJobStatus().equals(Constants.STOPPED)){
										// requeue task for a later time, put socket back into idleSocket
										idleSockets.add(slave);
										sortQueue.add(mapTask);
									} else {
										// OK to send task
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
	public int createJob(int mappers, int reducers,
						  String mapperDir, String mapperFile, String mapperName,
						  String reducerDir, String reducerFile, String reducerName,
						  String combinerDir, String combinerFile, String combinerName,
						  String[] fromPaths, String resultsDir){
		// create job
		int jobID = jobCount;
		Job job = new Job(jobID, mappers, reducers, workDirPath, fromPaths, resultsDir);
		job.setMapper(mapperDir, mapperFile, mapperName);
		job.setCombiner(combinerDir, combinerFile, combinerName);
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
		return jobID;
	}
	
	
	
}
