/**
 * The ServerSokcetIO class provides a wrapper that is easy to use to accept connections from ClinetSokcetIO objects.
 */
package networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import networking.SocketIOMessage;

public class ServerSocketIO {
	private Integer port;
	private ServerSocket serverSocket;
	private ArrayList<IncomingSocket> connections;
	
	/**
	 * Create a Server socket that is listening to connections on a certain port.
	 * @param port - the port the server will be listening on.
	 */
	public ServerSocketIO(Integer port){
		this.port = port;
		this.connections = new ArrayList<IncomingSocket>();
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
				while(true){
					try {
						Socket socket = serverSocket.accept();
						IncomingSocket inSocket = new IncomingSocket(socket);
						connections.add(inSocket);		
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
		
		public IncomingSocket(Socket socket){
			this.socket = socket;
			this.alive = true;
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
					InputStream in;
					OutputStream out;
					ObjectInputStream objInStream;
					ObjectOutputStream objOutStream;
					Object obj;
					try {
						//try communicating with the socket
						in = socket.getInputStream();
						out = socket.getOutputStream();
						objInStream = new ObjectInputStream(in);
						objOutStream = new ObjectOutputStream(out);
						while(alive){
							System.out.println("Hello???");
							if(!socket.isConnected()){
								//socket disconnected
								alive = false;
								System.out.println("Socket disconnected.");
							} else {
								//read in SokcetIOMessage object and retrieve message
								obj = objInStream.readObject();
								System.out.println("Got message: " + obj.toString());
								if(obj.getClass().getName() == "SocketIOMessage"){
									SocketIOMessage siom = (SocketIOMessage)obj;
									String message = siom.getMessage();
									//echo message
									System.out.println("Socket: " + message + ".");
								} else {
									//ignore message
								}
							}
						}
					} catch (IOException e) {
						System.out.println("Could not get socket input/output stream.");
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						System.out.println("Could not find class of object.");
						e.printStackTrace();
					}
				}
			};
			
			//start listening in a new thread
			new Thread(listen).start();
		}
		
	}
}

