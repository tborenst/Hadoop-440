package processManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import transactionaFileIO.tFile;
import util.Util;

import networking.SIOClient;
import networking.SIOCommand;

public class SlaveNode {
	private ArrayList<ThreadProcess> processes;
	private SIOClient clientSocket;
	private CommandPrompt prompt;
	private String serDirectoryPath;
	private Thread processCleaner;
	private boolean runProcessCleaning;
	private int nodeId;
	private int cleanProcessInterval;
	
	public SlaveNode(String serverHostname, int serverPort, String serDirectoryPath, int cleanProcessInterval) {
		this.nodeId = -1;
		this.processes = new ArrayList<ThreadProcess>();
		this.clientSocket = new SIOClient(serverHostname, serverPort);
		this.prompt = new CommandPrompt();
		this.processCleaner = new Thread(new Runnable() {
			@Override
			public void run() {
				cleanProcesses();
			}
		});
		this.runProcessCleaning = true;
		this.cleanProcessInterval = cleanProcessInterval;
		
		File serDir = new File(serDirectoryPath);
		if(serDir.exists() && serDir.isDirectory()) {
			this.serDirectoryPath = serDirectoryPath;
		}
		else {this.serDirectoryPath = "";}
		
		//Prompt Events
		prompt.on("ps", new SIOCommand() {
			public void run() {
				ps();
			}
		});
		
		prompt.on("quit", new SIOCommand() {
			public void run() {
				quit();
			}
		});
		
		prompt.on("addNewProcess", new SIOCommand() {
			public void run() {
				createNewProcess(args[0], args[1]);
			}
		});
		
		//ClientSocketIO Events
		clientSocket.on("nodeId", new SIOCommand() {
			public void run() {
				setNodeId(Integer.parseInt(args[0]));
			}
		});
		
		clientSocket.on("quit", new SIOCommand() {
			public void run() {
				quit();
			}
		});
		
		clientSocket.on("addNewProcess", new SIOCommand() {
			public void run() {
				addNewProcess(Integer.parseInt(args[0]), args[1], Util.destringifyArray(args[2]));
			}
		});
		
		clientSocket.on("addExistingProcess", new SIOCommand() {
			public void run() {
				//+processId+">"+processName+">"+serPath"
				addExistingProcess(Integer.parseInt(args[0]), args[1], args[2]);
			}
		});
		
		clientSocket.on("killProcess", new SIOCommand() {
			public void run() {
				killProcessById(Integer.parseInt(args[0]));
			}
		});
		
		clientSocket.on("moveProcess", new SIOCommand() {
			public void run() {
				moveProcessTo(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		});
		
		processCleaner.start();
	}
	
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	//-------------------------
	//----Prompt Management----
	//-------------------------
	
	/**
	 * void ps(void):
	 * Emits the locally running processes in a readable format.
	 */
	public void ps() {
		synchronized(processes){
			String string = "----------------------------------------------------\n"
					      + "Locally (physically) Running Processes:\n"
					      + "----------------------------------------------------\n";
			Iterator<ThreadProcess> iterator = processes.iterator();
			while(iterator.hasNext()){
				ThreadProcess process = iterator.next();
				string += "Process: " + process.getId() + " | " + process.getName() + "\n";
			}
			string += "----------------------------------------------------\n";
			prompt.emit(string);
		}
	}
	
	/**
	 * void quit(void):
	 * Closes the node.
	 */
	public void quit() {
		prompt.emit("Closing this slave node, all processes will be lost.");
		System.exit(1);
	}
	
	/**
	 * void createNewProcess(String processName, String args):
	 * For when a user creates a new process on a slave node.
	 * Asks the master to create a new process with processName using args.
	 * @param processName
	 * @param args
	 */
	public void createNewProcess(String processName, String args) {
		clientSocket.emit("addNewProcess>"+processName+">"+args);
		prompt.emit("Asked master to start process: "+processName);
	}
	
	
	//--------------------------
	//----Process Management----
	//--------------------------
	/**
	 * void addNewProcess(int id, String name, String[] args):
	 * Creates, adds and runs a new process with name and id with args.
	 * @param id
	 * @param name
	 * @param args
	 */
	public void addNewProcess(int id, String name, String[] args) {
		try {
			ThreadProcess p = new ThreadProcess(name, id, args);
			if(p != null) {
				processes.add(p);
				p.start();
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * void addExistingProcess(int id, String name, String serPath):
	 * Recovers, adds and runs the process with name and id, serialized at serPath.
	 * @param id
	 * @param name
	 * @param serPath
	 */
	public void addExistingProcess(int id, String name, String serPath) {
		ThreadProcess p = new ThreadProcess(serPath, id, name, true);
		if(p != null) {
			processes.add(p);
			p.start();
		}	
	}
	
	/**
	 * void moveProcessTo(int id, int newNodeId):
	 * Serializes the process with id and removes it from processes then asks the master node to send it to the node with newNodeId.
	 * @param id
	 * @param newNodeId
	 */
	public void moveProcessTo(int id, int newNodeId) {
		ThreadProcess p = removeProcessById(id);
		if(p != null) {
			tFile serFile = p.serialize(newSerFile());
			if(serFile != null) {
				clientSocket.emit("moveProcessCallback>"+id+">"+p.getName()+">"+serFile.getPath()+">"+newNodeId);
			}
			else {
				System.out.println("Failed to move process due to serialization: "+p.getName()+" "+id);
			}
		}
		else {
			System.out.println("Failed to find process: "+id);
		}
	}
	
	/**
	 * ThreadProcess killProcessById(int id):
	 * Kills the process with id and removes it from processes.
	 * @param id
	 * @return ThreadProcess
	 */
	public ThreadProcess killProcessById(int id) {
		ThreadProcess p = removeProcessById(id);
		if(p != null) {
			p.suspend();
		}
		return p;
	}
	
	/**
	 * ThreadProcess removeProcessById(int id):
	 * Finds and removes the process with id from processes.
	 * @param id
	 * @return ThreadProcess
	 */
	private ThreadProcess removeProcessById(int id) {
		Iterator<ThreadProcess> iterator = processes.iterator();
		ArrayList<ThreadProcess> tmpProcesses = new ArrayList<ThreadProcess>();
		ThreadProcess foundProcess = null;
		while(iterator.hasNext()) {
			ThreadProcess p = iterator.next();
			if(p.getId() == id) {foundProcess = p;}
			else {
				tmpProcesses.add(p);
			}
		}
		processes = tmpProcesses;
		return foundProcess;
	}
	
	/**
	 * tFile newSerFile(void):
	 * Creates a new file for serialization.
	 * Cycles until we can create a new file.
	 * @return
	 */
	private tFile newSerFile() {
		File serFileAttempt = new File(serDirectoryPath+makeSerFileName());
		while(serFileAttempt.exists()) {
			serFileAttempt = new File(serDirectoryPath+makeSerFileName());
		}
		
		return new tFile(serFileAttempt, true);
	}
	
	/**
	 * String makeSerFileName(void):
	 * Creates a random file name for serialization with extension ".ser".
	 * @return String
	 */
	private String makeSerFileName() {
		return new StringBuilder().append("prefix")
		        .append(System.currentTimeMillis()/1000000).append(UUID.randomUUID())
		        .append(".").append("ser").toString();
	}
	
	public void cleanProcesses() {
		while(this.runProcessCleaning) {
			synchronized(processes) {
				for(int i = 0; i < processes.size(); i++) {
					if(!processes.get(i).isAlive()) {
						ThreadProcess p = processes.remove(i);
						clientSocket.emit("removeDeadProcess>"+p.getId()+">"+nodeId);
					}
				}
			}
			try {
				Thread.sleep(cleanProcessInterval);
			} catch (InterruptedException e) {
				System.out.println("SlaveNode.cleanProcesses: sleep failed");
				e.printStackTrace();
			}
		}
		
		runProcessCleaning = true;
	}
	
}
