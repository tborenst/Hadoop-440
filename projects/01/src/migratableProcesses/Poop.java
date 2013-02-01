/**
 * The Poop class creates a new proxyManager object. It provides several public methods
 * to control the distributed system's processes from the master node.
 */

package migratableProcesses;

public class Poop implements Runnable {
	private ProxyManager proxyManager
	private boolean runLoadBalancing;
	
	public Poop() {
		this.proxyManager = new ProxyManager();
		this.runLoadBalancing = true;
		run();
	}
	
	/**
	 * 
	 * @param id
	 */
	public void addNode(String id) {
		Proxy free = proxyManager.addNode(id);
		loadBalanceFreeNode(free);
	}
	
	public void removeNode(String id) {
		proxyManager.removeNode(id);
	}
	
	public void loadBalance() {
		loadBalanceFreeNode(proxyManager.getLeastBusyProxy());
	}
	
	public void loadBalanceFreeNode(Proxy free) {
		Proxy busy = proxyManager.getBusiestProxy();
		if(busy.getId() != free.getId()) {
			Process p = busy.getRandomProcess();
			busy.socket.emit(busy.getId(), "moveProcess>"+p.getId()+">"+free.getId());
			//once ser file is saved, slave node emits to master to tell free node to pick up process
		}
	}
	
	public void runLoadBalancing() {
		while(runLoadBalancing) {
			loadBalance();
			Thread.sleep(100);
		}
		
		runLoadBalancing = true;
	}
	
	public void suspendLoadBalancing() {
		runLoadBalancing = false;
		while(!runLoadBalancing);
	}
}
