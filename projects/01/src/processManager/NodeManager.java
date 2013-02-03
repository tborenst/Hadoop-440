/**
 * The NodeManager class creates a new proxyManager object. It provides several public methods
 * to control the distributed system's processes from the master node. Note: socetId = nodeId, 
 */

package processManager;

import networking.SIOCommand;
import networking.SIOServer;

public class NodeManager {
	private static NodeProxyManager nodeProxyManager;
	private static boolean runLoadBalancing;
	private static SIOServer serverSocket;
	private static int loadBalanceThreshold;
	private static int loadBalanceInterval;
	private int processCounter;
	private static Prompt prompt;
	
	public NodeManager(int loadBalanceThreshold, int loadBalanceInterval) {
		this.nodeProxyManager = new NodeProxyManager();
		this.runLoadBalancing = true;
		this.serverSocket = new SIOServer(4313);
		this.loadBalanceThreshold = loadBalanceThreshold;
		this.loadBalanceInterval = loadBalanceInterval;
		this.processCounter = 0;
		this.prompt = new Prompt(); //check if process exists in prompt Class.forName
		
		//ServerSocketIO Events
		serverSocket.on("onconnection",  new SIOCommand(){
			public void run(){
				addNode(Integer.parseInt(args[0]));
			}
		});
		
		serverSocket.on("ondisconnect", new SIOCommand() {
			public void run() {
				removeDeadNode(Integer.parseInt(args[0]));
			}
		});
		
		serverSocket.on("addNewProcess", new SIOCommand(){
			public void run() {
				addNewProcess(args[0]);
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
				cleanDeadProcessProxy(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		});
		
		runLoadBalancing();
	}
	
	//ps
	
	//quit
	
	
	//-----------------------
	//----Node Management----
	//-----------------------
	/**
	 * void addNode(String id):
	 * Adds new node 
	 * @param id
	 */
	public void addNode(int nodeId) {
		NodeProxy free = nodeProxyManager.addNodeProxy(nodeId);
		loadBalanceWithNode(free);
	}
	
	/**
	 * void removeDeadNode(nodeId):
	 * Removes the NodeProxy with nodeId
	 * @param nodeId
	 */
	public void removeDeadNode(int nodeId) {
		nodeProxyManager.removeNodeProxyById(nodeId);
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
	public void addNewProcess(String processName) {
		NodeProxy free = nodeProxyManager.getLeastBusyNodeProxy();
		Boolean emitSent = serverSocket.emit(free.getId(), "addNewProcess>"+processName+">"+processCounter);
		if(emitSent) {
			free.addNewProcess(processCounter, processName);
			processCounter++;
		}
		else {
			addNewProcess(processName); //dangerous chance of endless recursive cycle
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
	public void moveProcessTo(int processId, String processName, String serPath, int nodeId) {
		serverSocket.emit(nodeId, "addExistingProcess>"+processId+">"+processName+">"+serPath);
		
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
		if(emitSent) {cleanDeadProcessProxy(nodeId, processId);}
	}
	
	/**
	 * void cleanDeadProcessProxy(int nodeId, int processId):
	 * Removes a dead ProcessProxy (processId) from NodeProxy (nodeId).
	 * @param nodeId
	 * @param processId
	 */
	public void cleanDeadProcessProxy(int nodeId, int processId) {
		NodeProxy p = nodeProxyManager.getNodeProxyById(nodeId);
		if(p != null) {p.removeProcessById(processId);}
	}
	
	/**
	 * void loadBalance():
	 * Load balances with the least busy node
	 */
	public void loadBalance() {
		NodeProxy free = nodeProxyManager.getLeastBusiesNodeProxy(); //may not be the node to which this process gets added to
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
		NodeProxy busy = nodeProxyManager.getBusiestNodeProxy();
		if(free != null && busy != null && busy.getId() != free.getId() &&
				busy.getNumProcesses() >= free.getNumProcesses()+loadBalanceThreshold) {
			
			ProcessProxy p = busy.getRandomProcess();
			if(p != null) {
				//once process is serialized, slave node emits to master to assign process to a free node
				Boolean emitSent = serverSocket.emit(busy.getId(), "moveProcess>"+p.getId());
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
			Thread.sleep(loadBalanceInterval);
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
