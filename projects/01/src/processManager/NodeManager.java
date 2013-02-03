/**
 * The NodeManager class creates a new proxyManager object. It provides several public methods
 * to control the distributed system's processes from the master node.
 */

package processManager;

import networking.SIOCommand;
import networking.ServerSocketIO;

public class NodeManager implements Runnable {
	private static ProxyManager nodeProxyManager;
	private static boolean runLoadBalancing;
	private static ServerSocketIO serverSocket;
	
	public NodeManager() {
		this.nodeProxyManager = new ProxyManager();
		this.runLoadBalancing = true;
		this.serverSocket = new ServerSocketIO(4313);
		
		//ServerSocketIO Events
		serverSocket.on("onconnection",  new SIOCommand(){
			public void run(){
				addNode(args[0]);
			}
		});
		
		serverSocket.on("addProcess", new SIOCommand() {
			public void run() {
				
			}
		})
		
		serverSocket.on("addNewProcess", new SIOCommand(){
			public void run() {
				addNewProcess(args[0], Integer.parseInt(args[1]));
			}
		});
		
		socketServer.on("removeDeadProcess", new SIOCommand() {
			public void run() {
				killProcess(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		});
		
		run();
	}
	
	
	/**
	 * void addNode(String id):
	 * @param id
	 */
	public void addNode(String id) {
		Proxy free = nodeProxyManager.addNode(id);
		loadBalanceFreeNode(free);
	}
	
	public void removeNode(String id) {
		nodeProxyManager.removeNode(id);
	}
	
	public void killProcess(int nodeId, int processId) {
		serverSocket.emit(nodeId, "killProcess>"+processId);
		removeProxyProcess(nodeId, processId);
	}
	
	public void removeProxyProcess(int nodeId, int processId) {
		nodeProxyManager.getProxyById(nodeId).removeProcessById(processId);
	}
	
	public void addExistingProcess(String processName, int processId, String serPath, int oldNodeId) {
		NodeProxy free = nodeProxyManager.getLeastBusyProxy();
		serverSocket.emit(free.getId(), "addExistingProcess>"+processName+">"+processId+">"+serPath);
		ThreadProcessProxy p = nodeProxyManager.getProxyById(oldNodeId).removeProcessById(processId);
		free.addProcess(p);
		//this should only be called for moving processes, so the proxy already has the existingProcess
		//ie no need to update the NodeProxy free
	}
	
	public void addNewProcess(String processName, int processId) {
		NodeProxy free = nodeProxyManager.getLeastBusyProxy();
		free.addNewProcess(processName, processId);
		serverSocket.emit(free.getId(), "newProcess>"+processName+">"+processId);
	}
	
	public void loadBalance() {
		NodeProxy free = nodeProxyManager.getLeastBusyProxy(); //may not be the node to which this process gets added to
		NodeProxy busy = nodeProxyManager.getBusiestProxy();
		if(busy.getId() != free.getId() && busy.getNumProcesses() >= free.getNumProcesses()+5) {
			
			ThreadProcessProxy p = busy.removeRandomProcess();
			serverSocket.emit(busy.getId(), "moveProcess>"+p.getId());
			//once ser file is saved, slave node emits to master to assign process to a free node
		}
	}
	
	public void runLoadBalancing() {
		while(runLoadBalancing) {
			loadBalance();
			Thread.sleep(5000);
		}
		
		runLoadBalancing = true;
	}
	
	public void suspendLoadBalancing() {
		runLoadBalancing = false;
		while(!runLoadBalancing);
	}

	@Override
	public void run() {
		runLoadBalancing();
	}
}
