/**
 * The ServerSokcetIO class provides a wrapper that is easy to use to accept connections from ClinetSokcetIO objects.
 */
package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSocketIO {
	private Integer port;
	private ServerSocket serverSocket;
	private ArrayList<IncomingSocket> connections;
	private Boolean alive;
	
	/**
	 * Create a Server socket that is listening to connections on a certain port.
	 * @param port - the port the server will be listening on.
	 */
	public ServerSocketIO(Integer port){
		this.port = port;
		this.connections = new ArrayList<IncomingSocket>();
		this.alive = true;
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Could not initiate a server socket on port: " + port + ".");
			e.printStackTrace();
		}
	}
	
	/**
	 * void listen(void):
	 * Start listening for incoming connections.
	 */
	public void listen(){
		Runnable listen = new Runnable(){
			@Override
			public void run(){
				while(alive){
					try {
						synchronized(serverSocket){
							Socket socket = serverSocket.accept();
							IncomingSocket inSocket = new IncomingSocket(socket);
							synchronized(connections){
								connections.add(inSocket);
							}
						}		
					} catch (IOException e) {
						System.out.println("Coud not accept connection on port: " + port + ".");
						e.printStackTrace();
					}
				}
			}
		};
		
		new Thread(listen).start();
	}
	
	/**
	 * void close(void):
	 * Closes the server socket.
	 */
	public void close(){
		try {
			alive = false;
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("Could not close server on port: " + port + ".");
			e.printStackTrace();
		}
	}
	
	/**
	 * class IncomingSocket:
	 * Holds a Socket and listens to its open connection.
	 * It is possible to bind Runnable objects to certain Strings on this socket for an "event-like" socket system.
	 */
	private class IncomingSocket{
		private Socket socket;
		private Boolean alive;
		private DataInputStream in;
		private DataOutputStream out;
		
		public IncomingSocket(Socket socket){
			this.socket = socket;
			this.alive = true;
			try {
				this.in = new DataInputStream(this.socket.getInputStream());
				this.out = new DataOutputStream(this.socket.getOutputStream());
			} catch (IOException e) {
				System.out.println("Could not get socket input/output stream.");
				e.printStackTrace();
			}
			go();
		}
		
		/**
		 * void go(void):
		 * Spawns a new Thread in which to communicate with the socket in a non-blocking manner.
		 */
		private void go(){
			//the listening process
			Runnable listen = new Runnable(){
				@Override
				public void run(){
					//all the object we need to communicate with the socket
					while(alive){
						if(!socket.isConnected()){
						//socket disconnected
							alive = false;
							System.out.println("Socket disconnected.");
						} else {
							//read in SokcetIOMessage object and retrieve message
							try {
								System.out.println(in.readUTF());
							} catch (IOException e) {
								System.out.print("Could not read from socket.");
								e.printStackTrace();
							}
						}
					}
				}
			};
			//start listening in a new thread
			new Thread(listen).start();
		}
	}
}

