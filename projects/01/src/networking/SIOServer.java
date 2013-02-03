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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class SIOServer {
	private int port;                              //the port to listen on
	private ServerSocket serverSocket;             //the server socket
	private ArrayList<IncomingSocket> connections; //incoming sockets
	private Boolean alive;                         //has the socket server been closed?
	private HashMap<String, SIOCommand> bindings;  //maps Strings to SIOCommand objects
	
	/**
	 * Create a Server socket that is listening to connections on a certain port.
	 * @param port - the port the server will be listening on.
	 */
	public SIOServer(int port){
		this.port = port;
		this.connections = new ArrayList<IncomingSocket>();
		this.bindings = new HashMap<String, SIOCommand>();
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
	 * void on(String, SIOCommand):
	 * Tell the server to run a certain SIOCommand upon receiving a certain message.
	 * Note: the SIOCommand bound to the String "connection" will get called every time 
	 * a new connection comes in with that socket's id.
	 * @param message - the message to active the Runnable.
	 * @param command - the SIOCOmmand to be run.
	 */
	public void on(String message, SIOCommand command){
		synchronized(bindings){
			bindings.put(message, command);
		}
	}
	
	/**
	 * void listen(void):
	 * Start listening for incoming connections.
	 */
	private void listen(){
		Runnable listen = new Runnable(){
			@Override
			public void run(){
				while(true){
					//clean up dead sockets every iteration
					cleanUp();
					synchronized(alive){
						if(!alive){
							break; //if server socket has been closed, break out of the loop
						}
					}
					try {
						//wait for a socket to connect
						Socket socket = serverSocket.accept();
						IncomingSocket inSocket = new IncomingSocket(socket);
						synchronized(connections){
							//add socket to list
							connections.add(inSocket);
						}		
						synchronized(bindings){
							//invoke the "connection" SIOCommand
							SIOCommand command = bindings.get("connection");
							if(command != null){
								String[] parameters = {String.valueOf(inSocket.getId())};
								command.parameters(parameters);
								try{
									command.run();
								} catch(Exception e){
									System.out.println("Failed to run command: connection.");
								}
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
	 * Boolean hashId(int):
	 * Returns true if there is a connected socket with a particular id.
	 * @param id - the id to check against.
	 */
	public Boolean hasId(int id){
		synchronized(connections){
			Iterator<IncomingSocket> sockets = connections.iterator();
			while(sockets.hasNext()){
				IncomingSocket socket = sockets.next();
				if(socket.getId() == id){
					return true;
				}
			}
			//no socket with this id found
			return false;
		}
	}
	
	/**
	 * void close(void):
	 * Closes the server socket.
	 */
	public void close(){
		synchronized(alive){
			alive = false;
			synchronized(serverSocket){
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.out.println("Could not close server on port: " + port + ".");
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * void broadcast(String):
	 * Send a message to all open incoming connections.
	 * @param message - the message to send to all incoming sockets
	 */
	public void broadcast(String message){
		synchronized(connections){
			Iterator<IncomingSocket> sockets = connections.iterator();
			while(sockets.hasNext()){
				IncomingSocket socket = (IncomingSocket) sockets.next();
				socket.sendMessage(message);
			}
		}
	}

	/**
	 * Boolean emit(int, String):
	 * Send a message to a particular socket with a certain id.
	 * If the socket is dead or is not found, emit returns false. It returns true otherwise.
	 * @param id - id of socket to send message to.
	 * @param message - message to be sent.
	 */
	public Boolean emit(int id, String message){
		synchronized(connections){
			Iterator<IncomingSocket> sockets = connections.iterator();
			//iterate over all sockets
			while(sockets.hasNext()){
				IncomingSocket socket = sockets.next();
				if(socket.getId() == id){
					//found socket, send message
					if(socket.isAlive()){
						socket.sendMessage(message);
						return true;
					} else {
						return false;
					}
				}
			}
			//socket not found
			return false;
		}
	}
	
	/**
	 * void cleanUp(void):
	 * Removes any dead IncomingSocket objects from the connections list
	 */
	private void cleanUp(){
		synchronized(connections){
			ArrayList<IncomingSocket> cleanedList = new ArrayList<IncomingSocket>(); //will contain only live sockets
			Iterator<IncomingSocket> sockets = connections.iterator();
			//iterate over all sockets
			while(sockets.hasNext()){
				IncomingSocket socket = sockets.next();
				if(socket.isAlive()){
					//socket is alive, add it to the new list
					cleanedList.add(socket);
				}
			}
			//replace old connection list with new, clean list
			connections = cleanedList;
		}
	}
	
	/**
	 * class IncomingSocket:
	 * Holds a Socket and listens to its open connection.
	 * It is possible to bind Runnable objects to certain Strings on this socket for an "event-like" socket system.
	 */
	private class IncomingSocket{
		private int id;
		private Socket socket;
		private Boolean alive;
		private DataInputStream in;
		private DataOutputStream out;
		
		public IncomingSocket(Socket socket){
			this.socket = socket;
			this.alive = true;
			this.id = 0 + (int)(Math.random() * ((1000 - 0) + 1)); //generate random id
			while(hasId(this.id)){
				//make sure socket's id is unique
				this.id = 0 + (int)(Math.random() * ((1000 - 0) + 1));
			}
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
					while(true){
						synchronized(socket){
							if(!socket.isConnected()){
							//socket disconnected
								synchronized(alive){
									alive = false;
									System.out.println("Socket disconnected.");
									break; //exit the loop if the socket isn't alive
								}
							} 
						}
						//read message from socket
						try {
							synchronized(in){
								String[] message = in.readUTF().split(">");
								String commandName = message[0]; //get command name
								synchronized(bindings){
									SIOCommand command = bindings.get(commandName);
									if(command != null){
										String[] parameters = Arrays.copyOfRange(message, 1, message.length); //cut commandName from message
										command.parameters(parameters); //pass parameters into command
										try{
											command.run(); //run command
										} catch(Exception e){
											System.out.println("Failed to run command: " + commandName + ".");
										}
									}
								}
							}
						} catch (IOException e) {
							System.out.print("Could not read input from socket. Killing socket.");
							synchronized(alive){
								alive = false;
							}
							return;
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
		
		/**
		 * Boolean isAlive(void):
		 * Returns true if the socket is alive, false if dead.
		 * @return - is the socket still alive?
		 */
		public Boolean isAlive(){
			return alive;
		}
		
		/**
		 * int getId(void):
		 * Returns the id of this socket.
		 * @return - the id of this socket.
		 */
		public int getId(){
			return id;
		}
	}
}

