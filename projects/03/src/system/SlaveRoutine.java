/**
 * The SlaveRoutine class will be called by any machine that is designated to be a slave of the system.
 * @author Tomer Borenstein
 */

package system;

import java.util.ArrayList;
import java.util.Iterator;

import fileio.Record;
import fileio.RecordsFileIO;
import api.Collector;
import api.IntWritable;
import api.Writable;
import util.Executer;
import util.Util;
import networking.SIOClient;
import networking.SIOCommand;

public class SlaveRoutine {
	private SIOClient sio;     // socket
	private Executer executer; // loads .class files
	private String workDir;    // working directory
	
	public SlaveRoutine(String hostname, int port, String workDir){
		this.sio = new SIOClient(hostname, port);
		this.executer = new Executer();
		// check that working directory is valid
		if(!Util.isValidDirectory(workDir)){
			try{
				throw new Throwable("Working directory invalid");
			} catch (Throwable e){
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
			@Override
			public void run(){
				Task task = (Task)object;
				if(task.getTaskType().equals(Constants.MAP)){
					performMapTask(task);
				} else if(task.getTaskType().equals(Constants.SORT)){
					performSortTask(task);
				} else if(task.getTaskType().equals(Constants.REDUCE)){
					performReduceTask(task);
				} else {
					sio.emit(Constants.TASK_ERROR, "Unrecognized task type");
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
		
		String combinerDir = task.getSecondaryDir();
		String combinerFile = task.getSecondaryFile();
		String combinerName = task.getSecondaryName();
		
		String mapOutputFile;
		if(combinerDir == null || combinerFile == null || combinerName == null){
			mapOutputFile = to[0];
		} else {
			mapOutputFile = workDir + "job" + task.getJobID() + "task" + task.getTaskID() + "intermediatefile1";
		}
		
		//try to execute the task, send back an error if failed
		try{
			// MAPPER
			Collector output = new Collector(mapOutputFile);
			RecordsFileIO reader = new RecordsFileIO(from[0], true, true);
			Class<?> mapperClass = executer.getClass(mapperDir, mapperFile, mapperName);
			Object mapObject = executer.instantaite(mapperClass, null);
			Record record;
			// execute mapper over and over until you've exhausted all records
			while((record = reader.readNextRecord("\n")) != null){
				Writable key = record.getKey();
				Writable[] values = record.getValues();
				for(int i = 0; i < values.length; i++){
					Object[] args = {key, values[i], output};
					executer.execute(mapObject, "map", args);
				}
			}
			output.dumpBuffer();
			output.close();
			
			// COMBINER
			if(combinerDir != null && combinerFile != null && combinerName != null){
				// sort and merge records from map
				RecordsFileIO combinerReader = new RecordsFileIO(mapOutputFile, true, true);
				combinerReader.sortRecords(workDir, "\n");;
				
				Collector combinerOutput = new Collector(to[0]);
				Class<?> combinerClass = executer.getClass(combinerDir, combinerFile, combinerName);
				Object combinerObject = executer.instantaite(combinerClass, null);
				
				Record combinerRecord;
				// executer combiner over and over until you've exhausted all records
				while((combinerRecord = combinerReader.readNextRecord("\n")) != null){
					Writable key = combinerRecord.getKey();
					Writable[] values = combinerRecord.getValues();
					Object[] args = {key, values, combinerOutput};
					executer.execute(combinerObject, "reduce", args); //TODO: COMBINE BOMEINE BOMEDNIE
				}
				combinerReader.delete();
				combinerOutput.dumpBuffer();
			}
			
			// done executing map, let the master know
			task.setStatus(Constants.COMPLETED);
			sio.emit(Constants.TASK_COMPLETE, task);
			
			reader.delete(); // delete input files once we're done
		} catch(Exception e){
			// error, let the server know
			sio.emit(Constants.TASK_ERROR, task);
		}
	}
	
	/**
	 * performSortTask - interprets and executes sort task
	 */
	public void performSortTask(Task task){
		String[] from = task.getPathFrom();
		String[] to = task.getPathTo();
		try{
			RecordsFileIO.mergeSortRecords(from, to, workDir, "\n", "\n", true); // true - delete source files
			task.setStatus(Constants.COMPLETED);
			sio.emit(Constants.TASK_COMPLETE, task);
		} catch (Exception e){
			sio.emit(Constants.TASK_ERROR, task);
		}
	}
	
	/**
	 * performReduceTask - interprets and executes reduce task
	 */
	public void performReduceTask(Task task){
		String[] from = task.getPathFrom();
		String[] to   = task.getPathTo();
		
		if(from.length != 1 || to.length != 1){
			sio.emit(Constants.TASK_ERROR, task);
		}
		
		String reducerDir  = task.getClassDir();
		String reducerFile = task.getClassFile();
		String reducerName = task.getClassName();
		
		//try to execute the task, send back an error if failed
		try{
			Collector output = new Collector(to[0]);
			RecordsFileIO reader = new RecordsFileIO(from[0], true, true);
			
			Class<?> reducerClass = executer.getClass(reducerDir, reducerFile, reducerName);
			Object reducerObject = executer.instantaite(reducerClass, null);
			
			Record record;
			// execute reducer over and over until you've exhausted all records
			while((record = reader.readNextRecord("\n")) != null){
				Writable key = record.getKey();
				Writable[] values = record.getValues();

				Object[] args = {key, values, output};
				executer.execute(reducerObject, "reduce", args);
			}
			
			// done executing reducer, let the master know
			output.dumpBuffer();
			task.setStatus(Constants.COMPLETED);
			sio.emit(Constants.TASK_COMPLETE, task);
			reader.delete(); // delete input files once you're done
		} catch(Exception e){
			// error, let the server know
			sio.emit(Constants.TASK_ERROR, task);
		}
	}
}
