package testing;

import networking.*;

public class Testing {
	
	public static void main(String[] args) throws InterruptedException{
		String hostname = "192.168.1.16";
		Integer port = 15237;
		
		final SIOServer server = new SIOServer(port);
		
		server.on("connection", new SIOCommand(){
			public void run(){
				System.out.println("New Incoming Socket, ID: " + args[0] + ".");
			}
		});
		
		server.on("disconnect", new SIOCommand(){
			public void run(){
				System.out.println("Socket Closed, ID: " + args[0] + ".");
			}
		});
		
		Thread.sleep(1000);
		
		final SIOClient client1 = new SIOClient(hostname, port);
		
		Thread.sleep(2000);
		
		client1.close();
	}
}
