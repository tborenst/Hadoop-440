package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocketIO {
	private String hostname;
	private Integer port;
	private Socket socket;
	private Boolean alive;
	private DataInputStream in;
	private DataOutputStream out;
	
	public ClientSocketIO(String hostname, Integer port){
		this.hostname = hostname;
		this.port = port;
		try {
			this.socket = new Socket(hostname, port);
			this.alive = true;
			this.in = new DataInputStream(this.socket.getInputStream());
			this.out = new DataOutputStream(this.socket.getOutputStream());
		} catch (UnknownHostException e) {
			System.out.print("Socket attempted to connect to unknown host: " + hostname + ".");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Sokcet failed to connect to host: " + hostname +" at port: " + port + ".");
			e.printStackTrace();
		}
		listen();
	}
	
	/**
	 * void listen(void):
	 * Spawn a thread to communicate with the server.
	 */
	private void listen(){
		Runnable listen = new Runnable(){
			@Override 
			public void run(){		
				
			}
		};
		new Thread(listen).start();
	}
	
	/**
	 * void close(void):
	 * Close socket.
	 */
	public void close(){
		synchronized(alive){
			this.alive = false;
		}
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Could not close socket.");
			e.printStackTrace();
		}
	}
}
