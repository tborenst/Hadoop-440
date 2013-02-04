package processManager;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import transactionaFileIO.tFile;
import util.Util;

import networking.SIOClient;

public class SlaveNode {
	private ArrayList<ThreadProcess> processes;
	private SIOClient clientSocket;
	//private CommandPrompt prompt;
	private String serDirectoryPath;
	
	public SlaveNode(String serverHostname, int serverPort, String serDirectoryPath) {
		this.processes = new ArrayList<ThreadProcess>();
		this.clientSocket = new SIOClient(serverHostname, serverPort);
		//prompt = new CommandPrompt();
		File serDir = new File(serDirectoryPath);
		if(serDir.exists() && serDir.isDirectory()) {
			this.serDirectoryPath = serDirectoryPath;
		}
		else {this.serDirectoryPath = "";}
	}
	
	//ps
	//quit
	//createNewProcess
	
	public void addNewProcess(int id, String name, String[] args) {
		ThreadProcess p = new ThreadProcess(name, id, args);
		if(p != null) {
			processes.add(p);
			p.start();
		}
	}
	
	
	public void addExistingProcess(int id, String name, String serPath) {
		ThreadProcess p = new ThreadProcess(name, id, serPath, true);
		if(p != null) {
			processes.add(p);
			p.start();
		}	
	}
	
	public void moveProcessTo(int id, int newNodeId) {
		ThreadProcess p = removeProcessById(id);
		tFile serFile = p.serialize(newSerFile());
		if(serFile != null) {
			clientSocket.emit("moveProcessCallback>"+id+">"+p.getName()+">"+serFile.getPath()+">"+newNodeId);
		}
		else {
			System.out.println("Failed to move process due to serialization: "+p.getName()+" "+p.getId()+" "+serFile.getPath());
		}
	}
	
	public ThreadProcess killProcessById(int id) {
		ThreadProcess p = removeProcessById(id);
		if(p != null) {
			p.suspend();
		}
		return p;
	}
	
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
	
	private tFile newSerFile() {
		File serFileAttempt = new File(serDirectoryPath+makeSerFileName());
		while(serFileAttempt.exists()) {
			serFileAttempt = new File(serDirectoryPath+makeSerFileName());
		}
		
		return new tFile(serFileAttempt, true);
	}
	
	private String makeSerFileName() {
		return new StringBuilder().append("prefix")
		        .append(System.currentTimeMillis()/1000000).append(UUID.randomUUID())
		        .append(".").append("ser").toString();
	}
	
	
}
