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
import fileio.UnableToAccessFileException;

import api.JobNotFoundException;
import api.JobStatus;

public class ClientRoutine {
	private CommandPrompt cmd;
	private SIOClient socket;
	private static String helpString = "To send a new job do 'run <path to config file>'.\n" +
									 "Check the status of a job with 'status <job id>'.\n" +
									 "Stop a job with 'stop <job id>'." +
									 "Start a stopped job with 'start <job id>'." +
									 "Exit or quit with 'quit'.\n" +
									 "And if you want to see this message again do 'help'.\n";
	
	public ClientRoutine(String hostname, int port) {
		this.cmd = new CommandPrompt();
		this.cmd.emit("Welcome to Vansi & Tomer's Map Reducer, step right up and run some tasks!\n" + helpString
					+ "Connecting to: " + hostname + ":" + port + "...");

		this.socket = new SIOClient(hostname, port);
		
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
						socket.emit(Constants.STOP_JOB, jobId);
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
						socket.emit(Constants.START_JOB, jobId);
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
					} catch (JsonParseException | JsonMappingException e) {
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
				System.exit(0);
			}
		});
	}
	
	private void initializeSocket() {
		/**
		 * Callback from server for job status request.
		 */
		socket.on(Constants.JOB_STATUS, new SIOCommand() {
			@Override
			public void run() {
				JobStatus jobStatus = (JobStatus) object;
				String statusStr = "Job " + jobStatus.getJobId() + " is " + jobStatus.getStatus() + ".";
				synchronized(cmd) {
					cmd.emit(statusStr);
				}
			}
		});
		
		/**
		 * Callback from server to confirm that a job was created.
		 */
		socket.on(Constants.JOB_REQUEST, new SIOCommand() {
			@Override
			public void run() {
				JobStatus jobStatus = (JobStatus) object;
				String statusStr = "Your job " + jobStatus.getJobId() + " has been added with status: " + jobStatus.getStatus() + ".";
				synchronized(cmd) {
					cmd.emit(statusStr);
				}
			}
		});
		
		socket.on(Constants.JOB_COMPLETE, new SIOCommand() {
			@Override
			public void run() {
				JobStatus jobStatus = (JobStatus) object;
				String statusStr = "Job " + jobStatus.getJobId() + " is " + Constants.COMPLETED + ".";
				synchronized(cmd) {
					cmd.emit(statusStr);
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
		if(socket != null && socket.isAlive()) {
			socket.emit(Constants.JOB_STATUS, jobId);
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
		if(socket != null && socket.isAlive()) {
			socket.emit(Constants.JOB_REQUEST, req);
		} else {
			throw new SocketFailureException();
		}
	}
	
	// ClientRoutine testing method
	public static void main(String[] args) {
		ClientRoutine c = new ClientRoutine("sdf.com", 23);
	}
}
