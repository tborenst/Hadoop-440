package networking;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSocketIO {
	private Integer port;
	private ServerSocket socket;
	private volatile ArrayList<Socket> connections;
	
	public ServerSocketIO(Integer port){
		this.port = port;
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not initiate server on port " + port + ".");
			e.printStackTrace();
		}
	}
	
	/**
	 * void listen(void):
	 * Start a new thread that is listening for connection requests.
	 */
	public void listen(){
		Runnable accept = new Runnable(){
			@Override
			public void run(){
				while(true){
					try {
						Socket connection = socket.accept();
						synchronized(connections){
							connections.add(connection);
						}
					} catch (IOException e){
						System.out.println("Could not accept connection on port " + port + ".");
						e.printStackTrace();
					}
				}
			}
		};
		
		new Thread(accept);
	}
	
	public void emit(Socket client, String message){
		try {
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("Could not emit message to client socket: " + client.toString() + ".");
			e.printStackTrace();
		}
	}
}
