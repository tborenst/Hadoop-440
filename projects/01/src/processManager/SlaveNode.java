package processManager;

import java.util.ArrayList;

import util.Util;

import networking.SIOClient;

public class SlaveNode {
	ArrayList<ThreadProcess> processes;
	SIOClient clientSocket;
	//CommandPrompt prompt;
	
	public SlaveNode(String serverHost, int serverPort) {
		this.processes = new ArrayList<ThreadProcess>();
		this.clientSocket = new SIOClient(serverHost, serverPort);
		//prompt = new CommandPrompt();
		
		
	}
	
	//ps
	//quit
	//createNewProcess
	
	//add process
	public void addNewProcess(int id, String name, String[] args) {
		ThreadProcess p = launchProcess(id, name, args);
		if(p != null) {processes.add(p);}
	}
	
	private ThreadProcess launchProcess(int id, String name, String[] args) {
		//Util.destringify();
	}
	
	//add existing process (from move)
	//remove process
	//move process
	
	
}
