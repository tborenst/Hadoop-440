/**
 * The NodeProxyManager class is a wrapper around an array of NodeProxy objects for convenience.
 */
package processManager;

import java.util.ArrayList;
import java.util.Iterator;

import processManager.NodeProxy.ProcessProxy;

public class NodeProxyManager {
	ArrayList<NodeProxy> proxies;
	
	public NodeProxyManager(){
		this.proxies = new ArrayList<NodeProxy>();
	}
	
	/**
	 * NodeProxy addNode(int):
	 * Create and add a new NodeProxy (with id) to the proxies list.
	 * @param id
	 * @return NodeProxy object that was created and added.
	 */
	NodeProxy addNode(int id){
		synchronized(proxies){
			NodeProxy proxy = new NodeProxy(id);
			proxies.add(proxy);
			return proxy;
		}
	}
	
	public String getProcessesAsString(){
		String string = "";
		synchronized(proxies){
			Iterator<NodeProxy> iterator = proxies.iterator();
			while(iterator.hasNext()){
				string += iterator.next().getProcessesAsString();
			}
		}
		return string;
	}
	
	public ProcessProxy getProcessById(int processId){
		synchronized(proxies){
			Iterator<NodeProxy> iterator = proxies.iterator();
			while(iterator.hasNext()){
				NodeProxy node = iterator.next();
				ProcessProxy process = node.getProcessById(processId);
				if(process != null){
					//process found
					return process;
				}
			}
			//process not found
			return null;
		}
	}
	
	public ProcessProxy removeProcessById(int processId){
		synchronized(proxies){
			Iterator<NodeProxy> iterator = proxies.iterator();
			while(iterator.hasNext()){
				NodeProxy node = iterator.next();
				ProcessProxy process = node.getProcessById(processId);
				if(process != null){
					//process found, remove it
					node.removeProcessById(processId);
					return process;
				}
			}
			return null;
		}
	}
	
	/**
	 * NodeProxy removeNodeById(int):
	 * Removes and returns a NodeProxy with a particular id.
	 * @param id
	 * @return NodeProxy object that was removed.
	 */
	NodeProxy removeNodeById(int id){
		synchronized(proxies){
			ArrayList<NodeProxy> cleanList = new ArrayList<NodeProxy>();
			Iterator<NodeProxy> iterator = proxies.iterator();
			NodeProxy toReturn = null;
			while(iterator.hasNext()){
				NodeProxy proxy = iterator.next();
				if(proxy.getId() != id){
					//add to new list
					cleanList.add(proxy);
				} else {
					//do not add to new list
					toReturn = proxy;
				}
			}
			return toReturn;
		}
	}
	
	/**
	 * NodeProxy getNodeById(int):
	 * Returns the node with the corresponding id.
	 */
	NodeProxy getNodeById(int id){
		synchronized(proxies){
			Iterator<NodeProxy> iterator = proxies.iterator();
			while(iterator.hasNext()){
				NodeProxy proxy = iterator.next();
				if(proxy.getId() == id){
					//found NodeProxy
					return proxy;
				}
			}
			//did not find NodeProxy
			return null;
		}
	}
	
	/**
	 * NodeProxy getBusiestNodeProxy(void):
	 * Returns the node with the most running processes.
	 */
	NodeProxy getBusiestNode(){
		synchronized(proxies){
			Iterator<NodeProxy> iterator = proxies.iterator();
			int max = 0;
			NodeProxy busiest = null;
			while(iterator.hasNext()){
				NodeProxy proxy = iterator.next();
				int numOfProcesses = proxy.getNumberOfProcesses();
				if(max < numOfProcesses){
					max = numOfProcesses;
					busiest = proxy;
				}
			}
			return busiest;
		}
	}
	
	/**
	 * NodeProxy getLeastBusyNodeProxy(void):
	 * Returns the node with the least running processes.
	 */
	NodeProxy getLeastBusyNode(){
		synchronized(proxies){
			Iterator<NodeProxy> iterator = proxies.iterator();
			if(!iterator.hasNext()){
				//no proxies
				return null;
			}
			NodeProxy leastBusy = iterator.next();
			int min = leastBusy.getNumberOfProcesses();
			while(iterator.hasNext()){
				NodeProxy proxy = iterator.next();
				int numOfProcesses = proxy.getNumberOfProcesses();
				if(numOfProcesses < min){
					min = numOfProcesses;
					leastBusy = proxy;
				}
			}
			return leastBusy;
		}
	}
}
