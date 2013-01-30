package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocketIO {
	private String hostname;
	private Integer port;
	private Socket socket;
	
	public ClientSocketIO(String hostname, Integer port){
		this.hostname = hostname;
		this.port = port;
		try {
			this.socket = new Socket(hostname, port);
		} catch (UnknownHostException e) {
			System.out.print("Socket attempted to connect to unknown host: " + hostname + ".");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Sokcet failed to connect to host: " + hostname +" at port: " + port + ".");
			e.printStackTrace();
		}
		go();
	}
	
	private void go(){
		Runnable listen = new Runnable(){
			@Override 
			public void run(){
				//all the objects we need to communicate with the server
				DataInputStream in;
				DataOutputStream out;
				try {
					//try communicating with the server
					in = new DataInputStream(socket.getInputStream());
					out = new DataOutputStream(socket.getOutputStream());
					out.writeUTF("Hello there server!");
				} catch (IOException e) {
					System.out.println("Could not get socket input/output stream.");
					e.printStackTrace();
				}
			}
		};
		new Thread(listen).start();
	}
}
