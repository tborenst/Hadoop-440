/**
 * The NodeManager class creates a new proxyManager object. It provides several public methods
 * to control the distributed system's processes as the master node. Note: nodeId = socketId and serialization files are temporary.
 */

package processManager;

import networking.SIOCommand;
import networking.ServerSocketIO;

public class NodeManager {
	private static ProxyManager nodeProxyManager;
	private static boolean runLoadBalancing;
	private static ServerSocketIO serverSocket;
	private static int loadBalanceThreshold;
	private static int loadBalanceInterval; //millisecs
	
	public NodeManager(int loadBalanceThreshold, int loadBalanceInterval) { //5, 5000
		this.nodeProxyManager = new ProxyManager();
		this.runLoadBalancing = true;
		this.serverSocket = new ServerSocketIO(4313);
		this.loadBalanceThreshold = loadBalanceThreshold;
		this.loadBalanceInterval = loadBalanceInterval;
		
		//ServerSocketIO Events
		serverSocket.on("onconnection",  new SIOCommand(){
			public void run(){
				addNode(Integer.parseInt(args[0]));
			}
		});
		
		//TODO: add on ondisconnect event to ServerSocketIO native events
		serverSocket.on("ondisconnect", new SIOCommand() {
			public void run() {
				removeNode(Integer.parseInt(args[0]));
			}
		});
		
		
		serverSocket.on("addNewProcess", new SIOCommand(){
			public void run() {
				addNewProcess(args[0]);
			}
		});
		
		serverSocket.on("moveProcessCallback", new SIOCommand() {
			public void run() {
				//String processName, int processId, String serPath, int nodeId
				moveProcessTo(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
			}
		});
		
		serverSocket.on("cleanDeadProcess", new SIOCommand() {
			public void run() {
				removeProxyProcess(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		});
		
		serverSocket.on("killProcess", new SIOCommand() {
			public void run() {
				killProcess(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		});
		
		runLoadBalancing();
	}
	
	//-------------------------
	//----Prompt Management----
	//-------------------------
	
	public String ps() {
		
	}
	
	public void quit() {
		System.out.println("running processes...");
		System.out.println(ps()+"\n");
		
		System.out.println("closing all slaves...");
		serverSocket.broadcast("quit");
		
		System.out.println("bye, bye... *I'll be baac*");
		System.exit(1);
	}
	
	
	//-----------------------
	//----Node Management----
	//-----------------------
	/**
	 * void addNode(int nodeId):
	 * Adds a new node to proxyManager from the, note socketId = nodeId
	 * @param nodeId
	 */
	public void addNode(int nodeId) {
		NodeProxy free = nodeProxyManager.addNode(nodeId);
		loadBalanceFreeNode(free);
	}
	
	/**
	 * void removeNode(int nodeId):
	 * Removes a node from proxyManager given a nodeId
	 * @param nodeId
	 */
	public void removeNode(int nodeId) {
		nodeProxyManager.removeNode(nodeId);
	}
	
	
	//--------------------------
	//----Process Management----
	//--------------------------
	
	/**
	 * void addNewProcess(String processName):
	 * Adds a new process the the least busy node (info from proxy)
	 * and emits
	 * @param processName
	 */
	public void addNewProcess(String processName) {
		NodeProxy free = nodeProxyManager.getLeastBusyProxy();
		ThreadProcessProxy p = free.addNewProcess(processName);
		serverSocket.emit(free.getId(), "newProcess>"+processName+">"+p.getId());
	}
	
	private void moveProcessTo(String processName, int processId, String serPath, int nodeId) {
		serverSocket.emit(nodeId, "addExistingProcess>"+processName+">"+processId+">"+serPath);
		
		//this should only be called for moving processes, so the proxy already has the existingProcess
		//ie no need to update the NodeProxy with nodeId
	}
	
	public void killProcess(int nodeId, int processId) {
		serverSocket.emit(nodeId, "killProcess>"+processId);
		removeProxyProcess(nodeId, processId);
	}
	
	public void removeProxyProcess(int nodeId, int processId) {
		//if process dies (naturally or user cmd) on slave, so no need to emit to slave
		nodeProxyManager.getProxyById(nodeId).removeProcessById(processId);
	}
	
	public void loadBalance() {
		NodeProxy free = nodeProxyManager.getLeastBusyProxy(); //may not be the node to which this process gets added to
		NodeProxy busy = nodeProxyManager.getBusiestProxy();
		if(busy.getId() != free.getId() && busy.getNumProcesses() >= free.getNumProcesses()+loadBalanceThreshold) {
			
			Boolean emitSuccess = serverSocket.emit(busy.getId(), "moveProcess>"+p.getId()+">"+free.getId());
			if(emitSuccess) {
				ThreadProcessProxy p = busy.removeRandomProcess();
				
				free.addExistingProcess(p);
				//once ser file is saved, slave node emits to master to assign process to the free node
			}
			
		}
	}
	
	public void runLoadBalancing() {
		while(runLoadBalancing) {
			loadBalance();
			Thread.sleep(loadBalanceInterval);
		}
		
		runLoadBalancing = true;
	}
	
	public void suspendLoadBalancing() {
		runLoadBalancing = false;
		while(!runLoadBalancing);
	}
}
