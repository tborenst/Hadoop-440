/**
 * The NodeManager class creates a new proxyManager object. It provides several public methods
 * to control the distributed system's processes from the master node. Note: socetId = nodeId, 
 */

package processManager;

import processManager.NodeProxy.ProcessProxy;
import networking.SIOCommand;
import networking.SIOServer;

public class NodeManager {
	private NodeProxyManager nodeProxyManager;
	private boolean runLoadBalancing;
	private SIOServer serverSocket;
	private int loadBalanceThreshold;
	private int loadBalanceInterval;
	private int processCounter;
	@SuppressWarnings("unused")
	private Thread loadBalanceThread;
	private CommandPrompt prompt;
	
	public NodeManager(int loadBalanceThreshold, int loadBalanceInterval, int port) {
		this.nodeProxyManager = new NodeProxyManager();
		this.runLoadBalancing = true;
		this.serverSocket = new SIOServer(port);
		this.loadBalanceThreshold = loadBalanceThreshold;
		this.loadBalanceInterval = loadBalanceInterval;
		this.processCounter = 0;
		this.prompt = new CommandPrompt(); //check if process exists in prompt Class.forName
		
		//CommandPrompt Events
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
				addNewProcess(args[0], args[1]);
			}
		});

		//ServerSocketIO Events
		serverSocket.on("connection",  new SIOCommand(){
			public void run(){
				addNode(Integer.parseInt(args[0]));
			}
		});
		
		serverSocket.on("disconnect", new SIOCommand() {
			public void run() {
				removeDeadNode(Integer.parseInt(args[0]));
			}
		});
		
		serverSocket.on("addNewProcess", new SIOCommand(){
			public void run() {
				addNewProcess(args[0], args[1]);
			}
		});
				
		serverSocket.on("moveProcessCallback", new SIOCommand() {
			public void run() {
				//int processId, String processName, String serPath, int nodeId
				moveProcessTo(Integer.parseInt(args[0]), args[1], args[2], Integer.parseInt(args[3]));
			}
		});
		
		serverSocket.on("removeDeadProcess", new SIOCommand() {
			public void run() {
				cleanDeadProcess(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		});
		
		this.loadBalanceThread = new Thread(new Runnable() {
			public void run() {
				runLoadBalancing();
			}
		});
	}
	
	public void ps() {
		prompt.emit(nodeProxyManager.getProcessesAsString());
	}
	
	//quit
	public void quit() {
		ps();
		prompt.emit("closing slave nodes...");
		serverSocket.broadcast("quit");
		prompt.emit("bye, bye... *I'll be back*");
		System.exit(1);
	}
	
	
	//-----------------------
	//----Node Management----
	//-----------------------
	/**
	 * void addNode(String id):
	 * Adds new node 
	 * @param id
	 */
	public void addNode(int nodeId) {
		NodeProxy free = nodeProxyManager.addNode(nodeId);
		serverSocket.emit(nodeId, "nodeId>"+nodeId);
		loadBalanceWithNode(free);
	}
	
	/**
	 * void removeDeadNode(nodeId):
	 * Removes the NodeProxy with nodeId
	 * @param nodeId
	 */
	public void removeDeadNode(int nodeId) {
		nodeProxyManager.removeNodeById(nodeId);
	}
	
	//--------------------------
	//----Process Management----
	//--------------------------
	/**
	 * void addNewProcess(String processName):
	 * Tells the least busiest node to add this new process (processName)
	 * if emit is successfully sent, edit the NodeProxy if not try again.
	 * @param processName
	 */
	public void addNewProcess(String processName, String args) {
		NodeProxy free = nodeProxyManager.getLeastBusyNode();
		if(free != null){
			Boolean emitSent = serverSocket.emit(free.getId(), "addNewProcess>"+processCounter+">"+processName+">"+args);
			if(emitSent) {
				free.addNewProcess(processCounter, processName);
				processCounter++;
				prompt.emit("launched process: "+processName+"on node: "+free.getId());
			}
			else {
				//addNewProcess(processName, args); //dangerous chance of endless recursive cycle
				prompt.emit("failed to launch process: "+processName);
			}
		} else {
			prompt.emit("No nodes are currently available to run proces: " + processName + ".");
		}
		
	}
	
	/**
	 * void moveProcessTo(int processId, String processName, String serPath, int nodeId):
	 * Move a process (processId) with processName (processName) serialized at serPath to the node with NodeId.
	 * Note: This doesn't edit NodeProxy since it has already been edited in loadBalance.
	 * Note: This is usually called from the moveProcessCallback.
	 * @param processId
	 * @param processName
	 * @param serPath
	 * @param nodeId
	 */
	public Boolean moveProcessTo(int processId, String processName, String serPath, int nodeId) {
		return serverSocket.emit(nodeId, "addExistingProcess>"+processId+">"+processName+">"+serPath);
		
		//this should only be called for moving processes, so the proxy already has the existingProcess
		//ie no need to update the NodeProxy with nodeId
	}
	
	/**
	 * void killProcess(int nodeId, int processId):
	 * Tells node (nodeId) to kill process (processId) and 
	 * then removes it from the NodeProxy (nodeId)
	 * @param nodeId
	 * @param processId
	 */
	public void killProcess(int nodeId, int processId) {
		Boolean emitSent = serverSocket.emit(nodeId, "killProcess>"+processId);
		if(emitSent) {cleanDeadProcess(nodeId, processId);}
	}
	
	/**
	 * ProcessProxy getProcessById(int):
	 * Looks through all proxy nodes and returns a process with a particular id.
	 * If no process with specified id is found, will return null.
	 * @param processId
	 * @return
	 */
	public ProcessProxy getProcessById(int processId) {
		return nodeProxyManager.getProcessById(processId);
	}
	
	/**
	 * void cleanDeadProcessProxy(int nodeId, int processId):
	 * Removes a dead ProcessProxy (processId) from NodeProxy (nodeId).
	 * @param nodeId
	 * @param processId
	 */
	public void cleanDeadProcess(int processId, int nodeId) {
		NodeProxy n = nodeProxyManager.getNodeById(nodeId);
		ProcessProxy p = null;
		if(n != null) {
			p = n.removeProcessById(processId);
		}
		else {
			p = nodeProxyManager.getProcessById(processId)
		}
		
		if(p != null) {
			prompt.emit("Terminated Process: "+p.getName()+" id: "+p.getId());
		}
	}
	
	/**
	 * void loadBalance():
	 * Load balances with the least busy node
	 */
	public void loadBalance() {
		NodeProxy free = nodeProxyManager.getLeastBusyNode(); //may not be the node to which this process gets added to
		loadBalanceWithNode(free);
	}
	
	/**
	 * void loadBalanceWithNode(NodeProxy free):
	 * Load balances with the given node (free), 
	 * and moves one process over if free has fewer processes 
	 * than busy by loadBalanceThreshold
	 * @param free
	 */
	public void loadBalanceWithNode(NodeProxy free) {
		NodeProxy busy = nodeProxyManager.getBusiestNode();
		if(free != null && busy != null && busy.getId() != free.getId() &&
				busy.getNumberOfProcesses() >= free.getNumberOfProcesses()+loadBalanceThreshold) {
			
			ProcessProxy p = busy.getRandomProcess();
			if(p != null) {
				//once process is serialized, slave node emits to master to assign process to a free node
				Boolean emitSent = serverSocket.emit(busy.getId(), "moveProcess>"+p.getId()+">"+free.getId());
				if(emitSent) {
					busy.removeProcessById(p.getId());
					free.addExistingProcess(p);
				}
			}
			
		}
	}
	
	/**
	 * void runLoadBalancing(void):
	 * Calls loadBalance() every loadBalanceInterval milliseconds.
	 */
	public void runLoadBalancing() {
		while(runLoadBalancing) {
			loadBalance();
			try {
				Thread.sleep(loadBalanceInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		runLoadBalancing = true;
	}
	
	/**
	 * void suspendLoadBalancing(void):
	 * Suspends the loop in runLoadBalancing().
	 */
	public void suspendLoadBalancing() {
		runLoadBalancing = false;
		while(!runLoadBalancing);
	}
}

