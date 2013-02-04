package processManager;

import java.util.ArrayList;

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
	//add existing process (from move)
	//remove process
	//move process
	
	
}
