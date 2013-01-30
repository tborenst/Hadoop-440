package testing;

import networking.*;

public class Testing {
	
	public static void main(String[] args) throws InterruptedException{
		System.out.println("Starting server up!");
		String host = "192.168.1.10";
		ServerSocketIO server = new ServerSocketIO(15237);
		server.listen();
		System.out.println(1000);
		System.out.println("Server is listening...");
		System.out.println("Start client up!");
		ClientSocketIO client = new ClientSocketIO(host, 15237);
	}
	
}
