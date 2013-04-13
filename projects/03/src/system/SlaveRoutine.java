/**
 * The SlaveRoutine class will be called by any machine that is designated to be a slave of the system.
 * @author Tomer Borenstein
 */

package system;

import java.io.File;

import fileio.Partitioner;
import fileio.Record;
import fileio.RecordsFileIO;
import api.Collector;
import api.Writable;
import util.Executer;
import networking.SIOClient;
import networking.SIOCommand;

public class SlaveRoutine {
	private SIOClient sio;
	private Executer executer;
	private String workDir;
	
	public SlaveRoutine(String hostname, int port, String workDir){
		this.sio = new SIOClient(hostname, port);
		this.executer = new Executer();
		// check that working directory exists
		if(!(new File(workDir).exists())){
			try {
				throw new Throwable("Working Directory Doesn't Exist");
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		this.workDir = workDir;
		handleRequests();
	}
	
	/**
	 * handleRequests - in charge of socket communication, interpreting and executing tasks
	 */
	private void handleRequests(){
		
		sio.on(Constants.TASK_REQUEST, new SIOCommand(){
			public void run(){
				Task task = (Task)object;
				if(task.getTaskType().equals(Constants.MAP)){
					performMapTask(task);
				} else if(task.getTaskType().equals(Constants.SORT)){
					performSortTask(task);
				} else if(task.getTaskType().equals(Constants.REDUCE)){
					performReduceTask(task);
				} else {
					sio.emit(Constants.TASK_ERROR, "Unrecognized Task type");
				}
			}
		});
	}
	
	/**
	 * performMapTask - interprets and executes map task
	 */
	public void performMapTask(Task task){
		String[] from = task.getPathFrom();
		String[] to   = task.getPathTo();
		
		if(from.length != 1 || to.length != 1){
			sio.emit(Constants.TASK_ERROR, "Map task has more than 1 'from' or 'to' path");
		}
		
		String mapperDir  = task.getClassDir();
		String mapperFile = task.getClassFile();
		String mapperName = task.getClassName();
		
		//try to execute the task, send back an error if failed
		try{
			Collector output = new Collector(to[0]);
			Partitioner partitioner = new Partitioner();
			
			Class<?> mapperClass = executer.getClass(mapperDir, mapperFile, mapperName);
			Object mapObject = executer.instantaite(mapperClass, null);
			
			Record record;
			// execute mapper over and over until you've exhausted all records
			while((record = partitioner.readNextRecord(from[0], "\n")) != null){
				Writable key = record.getKey();
				Writable[] values = record.getValues();
				for(int i = 0; i < values.length; i++){
					Object[] args = {key, values[i], output};
					executer.execute(mapObject, "map", args);
				}
			}
			
			// done executing map, let the master know
			task.setStatus(Constants.COMPLETED);
			sio.emit(Constants.TASK_COMPLETE, task);
		} catch(Exception e){
			// error, let the server know
			sio.emit(Constants.TASK_ERROR, "Failed to run Mapper");
		}
	}
	
	/**
	 * performSortTask - interprets and executes sort task
	 */
	public void performSortTask(Task task){
		String[] from = task.getPathFrom();
		String[] to = task.getPathTo();
		
		try{
			RecordsFileIO.mergeSortRecords(from, to, workDir, "\n", "\n");
			task.setStatus(Constants.COMPLETED);
			sio.emit(Constants.TASK_COMPLETE, task);
		} catch (Exception e){
			sio.emit(Constants.TASK_ERROR, "Failed to run Sort");
		}
	}
	
	/**
	 * performReduceTask - interprets and executes reduce task
	 */
	public void performReduceTask(Task task){
		String[] from = task.getPathFrom();
		String[] to   = task.getPathTo();
		
		if(from.length != 1 || to.length != 1){
			sio.emit(Constants.TASK_ERROR, "Reduce task has more than 1 'from' or 'to' path");
		}
		
		String reducerDir  = task.getClassDir();
		String reducerFile = task.getClassFile();
		String reducerName = task.getClassName();
		
		//try to execute the task, send back an error if failed
		try{
			Collector output = new Collector(to[0]);
			Partitioner partitioner = new Partitioner();
			
			Class<?> reducerClass = executer.getClass(reducerDir, reducerFile, reducerName);
			Object reducerObject = executer.instantaite(reducerClass, null);
			
			Record record;
			// execute reducer over and over until you've exhausted all records
			while((record = partitioner.readNextRecord(from[0], "\n")) != null){
				Writable key = record.getKey();
				Writable[] values = record.getValues();
				for(int i = 0; i < values.length; i++){
					Object[] args = {key, values[i], output};
					executer.execute(reducerObject, "map", args);
				}
			}
			
			// done executing reducer, let the master know
			task.setStatus(Constants.COMPLETED);
			sio.emit(Constants.TASK_COMPLETE, task);
		} catch(Exception e){
			// error, let the server know
			sio.emit(Constants.TASK_ERROR, "Failed to run Reducer");
		}
	}
}