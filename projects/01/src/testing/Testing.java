package testing;

import networking.*;

public class Testing {
	
	public static void main(String[] args) throws InterruptedException{
		String host = "192.168.1.10";
		Integer port = 1234;
		
		final ServerSocketIO server = new ServerSocketIO(port);
		final ClientSocketIO client = new ClientSocketIO(host, port);
		
		server.on("echo", new SIOCommand(){
			public void run(){
				System.out.println("ECHO: " + args[0]);
			}
		});
		
		client.on("heartbeat", new SIOCommand(){
			public void run(){
				client.emit("echo>I'm alive!!!");
			}
		});
		
		System.out.println("...");
		Thread.sleep(300);
		System.out.println("...");
		Thread.sleep(300);
		System.out.println("...");
		Thread.sleep(300);
		System.out.println("...");
		Thread.sleep(300);
		System.out.println("...");
		Thread.sleep(300);
		
		server.broadcast("heartbeat");
		
		
	}
}
