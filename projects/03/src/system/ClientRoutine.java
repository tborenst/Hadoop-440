package system;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import networking.SIOClient;
import networking.SIOCommand;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;


import client.SocketFailureException;

import api.JobNotFoundException;
import api.JobStatus;

public class ClientRoutine {
	private Map<Integer, String> jobStatuses; //map jobId to status strings
	private Integer numPendingStatusUpdates;
	private CommandPrompt cmd;
	private SIOClient socket;
	private static String helpString;
	
	public ClientRoutine(String hostname, int port) {
		jobStatuses = new HashMap<Integer, String>();
		numPendingStatusUpdates = 0;
		cmd = new CommandPrompt();
		helpString = "To send a new job do 'run <path to config file>'.\n" +
					 "Check the status of jobs with 'status all'.\n" +
					 "Exit or quit with 'quit'.\n" +
					 "And if you want to see this message again do 'help'.";
		cmd.emit("Welcome to Vansi & Tomer's Map Reducer, step right up and run some tasks!\n" + helpString);

		cmd.emit("Connecting to: " + hostname + ":" + port + ".");
		socket = new SIOClient(hostname, port);
		
		
		cmd.on("help", new SIOCommand() {
			@Override
			public void run() {
				cmd.emit("Not to worry where here to help. Just try these steps out.\n" + helpString);
			}
		});
		
		cmd.on("status", new SIOCommand() {
			@Override
			public void run() {
				String[] args = (String[]) object;
				if(args.length > 0) {
					if(args[0].equals("all")) {
						try {
							boolean pendingJobs = getStatusesOfJobs();
							if(!pendingJobs) {
								cmd.emit(statusesString());
							}
						}  catch (SocketFailureException e) {
							cmd.emit("ERROR: Unable to connect to server, use 'connect <hostname:port>'.");
						}
						return;
					}
				}
				
				cmd.emit("ERROR: Malformed expression, to get your job statuses use 'status all'.");
			}
		});
		
		/**
		 * Callback from server for jobStatus request.
		 */
		socket.on("jobStatus", new SIOCommand() {
			@Override
			public void run() {
				JobStatus jobStatus = (JobStatus) object;
				synchronized(jobStatuses) {
					if(jobStatuses.get(jobStatus.jobId) != null) {
						jobStatuses.put(jobStatus.jobId, jobStatus.status);
					}
					
					synchronized(numPendingStatusUpdates) {
						numPendingStatusUpdates--;
						
						if(numPendingStatusUpdates <= 0) {
							cmd.emit(statusesString());
							numPendingStatusUpdates = 0;
						}
					}
				}
			}
		});
		
		cmd.on("run", new SIOCommand() {
			@Override
			public void run() {
				String[] args = (String[]) object;
				if(args.length > 0) {
					String path = args[0];
					try {
						runMapReduce(path);
					} catch (JsonParseException | JsonMappingException e) {
						cmd.emit("ERROR: Invalid JSON formatting in config file at: " + path + ".");
					} catch (FileNotFoundException e) {
						cmd.emit("ERROR: Config file not found at: " + path + ".");
					} catch (InValidConfigFileException e) {
						cmd.emit("ERROR: Config file did not contain the correct or all the nessesary data at: " + path + ".");
					} catch (SocketFailureException e) {
						cmd.emit("ERROR: Unable to connect to server.");
					}
					
					return;
				}
				
				cmd.emit("ERROR: Malformed expression, to run a new map reduce task use 'run <path to config file>'.");
			}
		});
		
		/**
		 * Callback from server to confirm that a job was created.
		 */
		socket.on("jobCreated", new SIOCommand() {
			@Override
			public void run() {
				JobStatus jobStatus = (JobStatus) object;
				jobStatuses.put(jobStatus.jobId, jobStatus.status);
				cmd.emit("Your job as been added, jobId: " + jobStatus.jobId + "\tstatus: " + jobStatus.status);
			}
		});
		
		cmd.on("quit", new SIOCommand() {
			@Override
			public void run() {
				System.exit(0);
			}
		});
		
	}
 
	/**
	 * Sends requests for statuses of all jobs created by client.
	 * @return - true if a request was sent (if there are pending jobs) or false otherwise.
	 * @throws SocketFailureException
	 */
	private boolean getStatusesOfJobs() throws SocketFailureException {
		synchronized(jobStatuses) {
			Set<Integer> jobIds = jobStatuses.keySet();
			Iterator<Integer> it = jobIds.iterator();
			
			boolean pendingJobs = false;
			while(it.hasNext()) {
				int jobId = it.next();
				
				try {
					if(getJobStatus(jobId)) {
						pendingJobs = true;
					}
				} catch (JobNotFoundException e) {
					jobStatuses.remove(jobId);
				}
			}
			
			return pendingJobs;
		}
	}
	
	/**
	 * Sends a request for job status to the server.
	 * @param jobId
	 * @return boolean - true if job status request was sent, and false otherwise.
	 * @throws JobNotFoundException 
	 * @throws SocketFailureException 
	 */
	private boolean getJobStatus(int jobId) throws JobNotFoundException, SocketFailureException {
		String status = jobStatuses.get(jobId);
		
		if(status == null) {
			throw new JobNotFoundException();
		
		// TODO: change status != "COMPLETE" to use constants && change message obj (jobId) if needed
		} else if(status != "COMPLETE") {
			if(socket != null && socket.isAlive()) {
				socket.emit(Constants.JOB_STATUS, jobId);
				synchronized(numPendingStatusUpdates) {
					numPendingStatusUpdates++;
				}
				return true;
			} else {
				throw new SocketFailureException();
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Returns a nicely formated string showing the status of each job.
	 * @return
	 */
	private String statusesString() {
		String statuses = "JobId\t|Status\n";
		synchronized(jobStatuses) {
			Set<Integer> jobIds = jobStatuses.keySet();
			Iterator<Integer> it = jobIds.iterator();
			
			if(!it.hasNext()) {
				statuses = "You have not created any jobs.";
			}
			
			while(it.hasNext()) {
				int jobId = it.next();
				String status = jobStatuses.get(jobId);
				if(status != null) {
					statuses += jobId + "\t" + status + "\n";
				}
			}
		}
		
		return statuses;
	}
	
	/**
	 * To send the map reduce request to the server.
	 * @param configFilePath
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws FileNotFoundException
	 * @throws InValidConfigFileException
	 * @throws SocketFailureException
	 */
	private void runMapReduce(String configFilePath) throws JsonParseException, JsonMappingException, FileNotFoundException, InValidConfigFileException, SocketFailureException {
		Request req = Request.constructFromFile(configFilePath);
		if(socket != null && socket.isAlive()) {
			socket.emit(Constants.JOB_REQUEST, req);
		} else {
			throw new SocketFailureException();
		}
	}
	
	// ClientRoutine testing method
	public static void main(String[] args) {
		ClientRoutine c = new ClientRoutine("vansivallab.com", 23);
	}
}
