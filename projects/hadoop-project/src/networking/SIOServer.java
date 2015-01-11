package networking;

import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SIOServer extends SIOSocket{
	private String hostname;
	private int port;
	private ServerSocket serverSocket;
	private ArrayList<IncomingSocket> sockets;
	int socketCount;
	private HashMap<String, SIOCommand> bindings;
	
	public SIOServer(int port){
		try {
			//find my IP address
			this.hostname = java.net.InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			//can't find IP address
			this.hostname = "Unknown";
		}
		this.port = port;
		this.sockets = new ArrayList<IncomingSocket>();
		this.socketCount = 0;
		this.bindings = new HashMap<String, SIOCommand>();
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		listen(); 
	}
	
	//listen for incoming connections
	private void listen(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(true){
					//handle incoming connection
					try {
						Socket socket;
						socket = serverSocket.accept();
						int id = socketCount++;
						IncomingSocket inSocket = new IncomingSocket(socket, id);
						synchronized(sockets){
							sockets.add(inSocket);
						}
						//call "connection" SIOCommand
						synchronized(bindings){
							SIOCommand command = bindings.get("connection");
							if(command != null){
								command.passObject(id); //call "connection" with new socket's id
								command.passSocket(inSocket);
								try{
									command.run();
								} catch (Exception e){
									e.printStackTrace();
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}	
		}).start();
	}
	
	/**
	 * Send a message 'message' with an object 'object' to all connected sockets.
	 */
	public void broadcast(String message, Object object){
		SIOPacket packet = new SIOPacket(message, object);
		synchronized(sockets){
			Iterator<IncomingSocket> itr = sockets.iterator();
			while(itr.hasNext()){
				IncomingSocket socket = itr.next();
				socket.sendPacket(packet);
			}
		}
	}
	
	/**
	 * Send a message 'message' with an object 'object' to socket with id 'id'.
	 */
	public void emit(int id, String message, Object object){
		SIOPacket packet = new SIOPacket(message, object);
		synchronized(sockets){
			Iterator<IncomingSocket> itr = sockets.iterator();
			while(itr.hasNext()){
				IncomingSocket socket = itr.next();
				if(socket.getId() == id){
					socket.sendPacket(packet);
					return;
				}
			}
		}
	}
	
	/**
	 * Bind an SIOCommand to a particular String 'message'.
	 */
	public void on(String message, SIOCommand command){
		synchronized(bindings){
			bindings.put(message, command);
		}
	}
	
	/**
	 * Returns this server's hostname
	 */
	public String getHostname(){
		return hostname;
	}
	
	/**
	 * Returns this server's port
	 */
	public int getPort(){
		return port;
	}
	
	/**
	 * Clean up dead sockets.
	 */
	protected void cleanUp(){
		synchronized(sockets){
			ArrayList<IncomingSocket> liveSockets = new ArrayList<IncomingSocket>();
			Iterator<IncomingSocket> itr = sockets.iterator();
			while(itr.hasNext()){
				IncomingSocket socket = itr.next();
				if(socket.isAlive()){
					liveSockets.add(socket);
				}
			}
			sockets = liveSockets;
		}
	}
	
	
	public class IncomingSocket extends SIOSocket{
		private int id;
		private Socket socket;
		private ObjectInputStream objIn;
		private ObjectOutputStream objOut;
		private Boolean alive = true;
		
		public IncomingSocket(Socket socket, int id){
			this.socket = socket;
			this.id = id;
			this.alive = true;
			try{
				this.objOut = new ObjectOutputStream(socket.getOutputStream());
				this.objIn = new ObjectInputStream(socket.getInputStream());
			} catch(IOException e) {
				e.printStackTrace();
			}
			listen();
		}
		
		/**
		 * Listen for packets from the client that corresponds to this IncomingSocket.
		 */
		private void listen(){
			final IncomingSocket self = this;
			new Thread(new Runnable(){
				@Override
				public void run() {
					while(true){
						try {
							//receive SIOPacket and unpack it
							SIOPacket packet = (SIOPacket)objIn.readObject();
							String message = packet.getMessage();
							Object object = packet.getObject();
							//find and run corresponding SIOCommand
							synchronized(bindings){
								SIOCommand command = bindings.get(message);
								if(command != null){
									command.passObject(object);
									command.passSocket(self);
									if(packet.isBlocking()){
										//this is a blocking request from the client
										command.passRequestId(packet.getRequestId());
									}
									try{
										command.run();
									} catch (Exception e){
										e.printStackTrace();
									}
								}
							}
						} catch (IOException e) {
							disconnect();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
		
		/**
		 * Called when IncomingSocket is disconnected. Run's the "disconnect" SIOCommand.
		 */
		private void disconnect(){
			synchronized(alive){
				alive = false;
			}
			//call disconnect SIOCommand
			synchronized(bindings){
				SIOCommand command = bindings.get("disconnect");
				if(command != null){
					command.passObject(id); //pass this socket's id to command
					command.passSocket(this);
					try{
						command.run();
					} catch (Exception exn){
						exn.printStackTrace();
					}
				}
			}
		}
		
		/**
		 * Send a String 'message' with the Object 'object' to this socket.
		 */
		@Override
		public void emit(String message, Object object){
			SIOPacket packet = new SIOPacket(message, object);
			sendPacket(packet);
		}
		
		/**
		 * Respond to a blocking request with id 'requestId', with a String 'message' and
		 * an Object 'object'.
		 */
		@Override
		public void respond(int requestId, Object object){
			SIOPacket packet = new SIOPacket("", object);
			packet.setBlocking(true); //this is a response to a blocking request
			packet.setRequestId(requestId); //blocking request with id requestId
			sendPacket(packet);
		}
		
		
		/**
		 * Send an SIOPacket to this socket.
		 */
		public void sendPacket(SIOPacket packet){
			synchronized(objOut){
				try {
					objOut.writeObject(packet);
				} catch (IOException e) {
					//socket disconnected
					disconnect();
				}
			}
		}
		
		/**
		 * Returns true if socket is alive, false if dead.
		 */
		@Override
		public Boolean isAlive(){
			synchronized(alive){
				return alive;
			}
		}
		
		/**
		 * Returns IncomingSocket's id.
		 */
		public int getId(){
			return id;
		}
		
	}
}
