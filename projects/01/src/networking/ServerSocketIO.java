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
import java.util.HashMap;

public class ServerSocketIO {
	private Integer port;                          //the port to listen on
	private ServerSocket serverSocket;             //the server socket
	private ArrayList<IncomingSocket> connections; //incoming sockets
	private Boolean alive;                         //has the socket server been closed?
	private HashMap<String, Runnable> bindings;    //maps Strings to Runnable objects
	
	/**
	 * Create a Server socket that is listening to connections on a certain port.
	 * @param port - the port the server will be listening on.
	 */
	public ServerSocketIO(Integer port){
		this.port = port;
		this.connections = new ArrayList<IncomingSocket>();
		this.bindings = new HashMap<String, Runnable>();
		try {
			this.serverSocket = new ServerSocket(port);
			this.alive = true;
		} catch (IOException e) {
			System.out.println("Could not initiate a server socket on port: " + port + ".");
			e.printStackTrace();
		}
		listen();
	}
	
	
	/**
	 * void listen(void):
	 * Start listening for incoming connections.
	 */
	private void listen(){
		Runnable listen = new Runnable(){
			@Override
			public void run(){
				while(alive){
					try {
						Socket socket = serverSocket.accept();
						IncomingSocket inSocket = new IncomingSocket(socket);
						synchronized(connections){
							connections.add(inSocket);
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
	 * void broadcast(String):
	 * Send a message to all open incoming connections.
	 * @param message - the message to send to all incoming sockets
	 */
	public void broadcast(String message){
		
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
			listen();
		}
		
		/**
		 * void listen(void):
		 * Spawns a new Thread in which to communicate with the socket in a non-blocking manner.
		 */
		private void listen(){
			//the listening process
			Runnable listen = new Runnable(){
				@Override
				public void run(){
					//all the object we need to communicate with the socket
					while(true){
						if(!socket.isConnected()){
						//socket disconnected
							synchronized(alive){
								alive = false;
								System.out.println("Socket disconnected.");
								break; //exit the loop if the socket isn't alive
							}
						} else {
							//read in SokcetIOMessage object and retrieve message
							try {
								synchronized(in){
									System.out.println(in.readUTF());
								}
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
		
		/**
		 * void sendMessage(String):
		 * Send a message to this individual socket.
		 * @param message - the message to be sent.
		 */
		public void sendMessage(String message){
			synchronized(alive){
				if(alive){
					synchronized(out){
						try {
							out.writeUTF(message);
						} catch (IOException e) {
							System.out.println("Could not send message ['" + message + "'] to socket.");
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}

