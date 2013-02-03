<<<<<<< HEAD
package testing;

import networking.*;

public class Testing {
	
	public static void main(String[] args) throws InterruptedException{
		String hostname = "192.168.1.10";
		Integer port = 15237;
		
		final SIOServer server = new SIOServer(port);
		
		server.on("connection", new SIOCommand(){
			public void run(){
				System.out.println("New Incoming Socket, ID: " + args[0] + ".");
			}
		});
		
		Thread.sleep(1000);
		
		final SIOClient client1 = new SIOClient(hostname, port);
		final SIOClient client2 = new SIOClient(hostname, port);
		final SIOClient client3 = new SIOClient(hostname, port);
		final SIOClient client4 = new SIOClient(hostname, port);
		final SIOClient client5 = new SIOClient(hostname, port);
	}
}
=======
package testing;

import networking.*;

public class Testing {
	
	private Object lock = new Object();
	
	public void A(){
		synchronized(lock){
			System.out.println("We are in A!");
			B();
		}
	}
	
	public void B(){
		synchronized(lock){
			System.out.println("We are in B!");
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException{
		Testing test = new Testing();
		test.A();
	}
}
>>>>>>> e1e6182407c31671cce09ba18abbdf4be4b0a61a
