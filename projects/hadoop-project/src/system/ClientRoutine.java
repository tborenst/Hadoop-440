package system;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import networking.SIOClient;
import networking.SIOCommand;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;



import system.SocketFailureException;
import fileio.UnableToAccessFileException;

import api.JobStatus;

public class ClientRoutine {
	private CommandPrompt cmd;
	private ArrayList<Integer> jobIds;
	private SIOClient sioSocket;
	private static String helpString = "To send a new job do 'run <path to config file>'.\n" +
									 "Check the status of a job with 'status <job id>'.\n" +
									 "Stop a job with 'stop <job id>'." +
									 "Start a stopped job with 'start <job id>'." +
									 "Exit or quit with 'quit'.\n" +
									 "And if you want to see this message again do 'help'.\n";
	
	
	public ClientRoutine(String hostname, int port) {
		this.cmd = new CommandPrompt();
		System.out.println("Welcome to Vansi & Tomer's Map Reducer, step right up and run some tasks!\n" + helpString
					+ "Connecting to: " + hostname + ":" + port + "...");
		this.jobIds = new ArrayList<Integer>();
		
		this.sioSocket = null;
		try {
			this.sioSocket = new SIOClient(hostname, port);
		} catch(Exception e) {
			cmd.emit("Unable to connect to server at: " + hostname + ":" + port +".");
			return;
		}
		cmd.emit("Connected to server at: " + hostname + ":" + port +".");
		
		initializeCommandPrompt();
		initializeSocket();
		
	}

	
	private void initializeCommandPrompt() {
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
					try {
						getJobStatus(Integer.parseInt(args[0]));
					}  catch (SocketFailureException e) {
						synchronized(cmd) {
							cmd.emit("ERROR: Unable to connect to server, use 'connect <hostname:port>'.");
						}	
					} catch (NumberFormatException e) {
						synchronized(cmd) {
							cmd.emit("ERROR: Malformed expression, to get your job statuses use 'status <job id>', job id is an integer.");
						}
					}
					return;
				}
				
				synchronized(cmd) {
					cmd.emit("ERROR: Malformed expression, to get your job statuses use 'status <job id>'.");
				}
			}
		});
		
		cmd.on("stop", new SIOCommand() {
			@Override
			public void run() {
				String[] args = (String[]) object;
				if(args.length > 0) {
					try {
						int jobId = Integer.parseInt(args[0]);
						sioSocket.emit(Constants.STOP_JOB, jobId);
					} catch (NumberFormatException e) {
						synchronized(cmd) {
							cmd.emit("ERROR: Malformed expression, to stop your job use 'stop <job id>', job id is an integer.");
						}
					}
					return;
				}
				
				synchronized(cmd) {
					cmd.emit("ERROR: Malformed expression, to stop your job use 'stop <job id>'.");
				}
			}
		});
		
		cmd.on("start", new SIOCommand() {
			@Override
			public void run() {
				String[] args = (String[]) object;
				if(args.length > 0) {
					try {
						int jobId = Integer.parseInt(args[0]);
						sioSocket.emit(Constants.START_JOB, jobId);
					} catch (NumberFormatException e) {
						synchronized(cmd) {
							cmd.emit("ERROR: Malformed expression, to stop your job use 'stop <job id>', job id is an integer.");
						}
					}
					return;
				}
				
				synchronized(cmd) {
					cmd.emit("ERROR: Malformed expression, to stop your job use 'stop <job id>'.");
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
					} catch (JsonParseException jpe) {
						synchronized(cmd) {
							cmd.emit("ERROR: Invalid JSON formatting in config file at: " + path + ".");
						}
					} catch (JsonMappingException jme) {
						synchronized(cmd) {
							cmd.emit("ERROR: Invalid JSON formatting in config file at: " + path + ".");
						}
					} catch (InValidConfigFileException e) {
						synchronized(cmd) {
							cmd.emit("ERROR: Config file did not contain the correct or all the nessesary data at: " + path + ".");
						} 
					} catch (SocketFailureException e) {
						synchronized(cmd) {
							cmd.emit("ERROR: Unable to connect to server.");
						}
					} catch (UnableToAccessFileException e) {
						synchronized(cmd) {
							cmd.emit("ERROR: Unable to access config file at: " + path + ".");
						} 
					} catch (FileNotFoundException e) {
						synchronized(cmd) {
							cmd.emit("ERROR: Config file not found or unable to access file at: " + path + ".");
						}
					}
					
					return;
				}
				
				synchronized(cmd) {
					cmd.emit("ERROR: Malformed expression, to run a new map reduce task use 'run <path to config file>'.");
				}
			}
		});
		
		cmd.on("quit", new SIOCommand() {
			@Override
			public void run() {
				sioSocket.on(Constants.JOB_STATUS, new SIOCommand() {});
				sioSocket.on(Constants.JOB_REQUEST, new SIOCommand() {});
				sioSocket.on(Constants.JOB_COMPLETE, new SIOCommand() {});
				synchronized(jobIds) {
					for(int j = 0; j < jobIds.size(); j++) {
						try {
							sioSocket.emit(Constants.STOP_JOB, jobIds.get(j));
						} catch(Exception e) {
							// we don't care if there are any exceptions since the user is exiting
						}
					}
				}
				System.exit(0);
			}
		});
	}
	
	private void initializeSocket() {
		/**
		 * Callback from server for job status request.
		 */
		sioSocket.on(Constants.JOB_STATUS, new SIOCommand() {
			@Override
			public void run() {
				JobStatus jobStatus = (JobStatus) object;
				String statusStr = "Job " + jobStatus.getJobId() + " is " + jobStatus.getStatus() + ".";
				synchronized(cmd) {
					cmd.emit(statusStr);
				}
				
				if(jobStatus.getStatus().equals(Constants.FAILED) || jobStatus.getStatus().equals(Constants.COMPLETED)) {
					synchronized(jobIds) {
						int idx = jobIds.indexOf(jobStatus.getJobId());
						if(idx != -1) {
							jobIds.remove(idx);
						}
					}
				}
			}
		});
		
		/**
		 * Callback from server to confirm that a job was created.
		 */
		sioSocket.on(Constants.JOB_REQUEST, new SIOCommand() {
			@Override
			public void run() {
				JobStatus jobStatus = (JobStatus) object;
				String statusStr = "Your job " + jobStatus.getJobId() + " has been added with status: " + jobStatus.getStatus() + ".";
				synchronized(cmd) {
					cmd.emit(statusStr);
				}
				
				if(!jobStatus.getStatus().equals(Constants.FAILED)) {
					synchronized(jobIds) {
						jobIds.add(jobStatus.getJobId());
					}
				}
			}
		});
		
		sioSocket.on(Constants.JOB_COMPLETE, new SIOCommand() {
			@Override
			public void run() {
				JobStatus jobStatus = (JobStatus) object;
				String statusStr = "Job " + jobStatus.getJobId() + " is " + Constants.COMPLETED + ".";
				synchronized(cmd) {
					cmd.emit(statusStr);
				}
				
				synchronized(jobIds) {
					int idx = jobIds.indexOf(jobStatus.getJobId());
					if(idx != -1) {
						jobIds.remove(idx);
					}
				}
			}
		});
	}

	/**
	 * Sends a request for job status to the server.
	 * @param jobId
	 * @throws SocketFailureException 
	 */
	private void getJobStatus(int jobId) throws SocketFailureException {
		if(sioSocket != null && sioSocket.isAlive()) {
			sioSocket.emit(Constants.JOB_STATUS, jobId);
		} else {
			throw new SocketFailureException();
		}
	}
	
	/**
	 * To send the map reduce request to the server.
	 * @param configFilePath
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws FileNotFoundException
	 * @throws InValidConfigFileException
	 * @throws SocketFailureException
	 * @throws UnableToAccessFileException 
	 */
	private void runMapReduce(String configFilePath) throws JsonParseException, JsonMappingException, FileNotFoundException, InValidConfigFileException, SocketFailureException, UnableToAccessFileException {
		Request req = Request.constructFromFile(configFilePath);
		if(sioSocket != null && sioSocket.isAlive()) {
			sioSocket.emit(Constants.JOB_REQUEST, req);
		} else {
			throw new SocketFailureException();
		}
	}
	
	// ClientRoutine testing method
	public static void main(String[] args) {
		ClientRoutine c = new ClientRoutine("google.com", 8080);
	}
}
