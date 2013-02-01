package testing;

import networking.*;

public class Testing {
	
	public static void main(String[] args) throws InterruptedException{
		String hostname = "unix3.andrew.cmu.edu";
		Integer port = 15237;
		
		final ClientSocketIO client = new ClientSocketIO(hostname, port);

		client.emit("echo>lalalala");
	}
}
