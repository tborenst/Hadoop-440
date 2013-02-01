/**
 * The ClientSocketIO class is the client-side wrapper for SocketIO. It can connect to a server, listen to events from that server, and bind to
 * SIOCommand objects that will be run on cue from certain events.
 */
package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;

public class ClientSocketIO {
	private String hostname;                      //the hostname to connect to
	private Integer port;                         //the port to connect to
	private Socket socket;                        //the socket object
	private Boolean alive;                        //has the socket been disconnected?
	private DataInputStream in;                   
	private DataOutputStream out; 
	private HashMap<String, SIOCommand> bindings; //maps Strings to SIOCommand objects
	
	public ClientSocketIO(String hostname, Integer port){
		this.hostname = hostname;
		this.port = port;
		this.bindings = new HashMap<String, SIOCommand>();
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
	 * void on(String, SIOCommand):
	 * Tell the client to run a certain SIOCommand upon receiving a certain message.
	 * @param message - the message to active the Runnable.
	 * @param command - the SIOCOmmand to be run.
	 */
	public void on(String message, SIOCommand command){
		synchronized(bindings){
			bindings.put(message, command);
		}
	}
	
	/**
	 * void emit(String):
	 * Send a message to the server. 
	 * In order to invoke functions on the server, use the following format:
	 * emit("funcName>arg1>arg2>...>arg3");
	 * @param message - the message to be sent to the server.
	 */
	public void emit(String message){
		synchronized(out){
			try {
				out.writeUTF(message);
			} catch (IOException e) {
				System.out.println("Could not emit message: " + message + ".");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * void listen(void):
	 * Spawn a thread to communicate with the server.
	 */
	private void listen(){
		Runnable listen = new Runnable(){
			@Override 
			public void run(){		
				while(true){
					synchronized(socket){
						if(!socket.isConnected()){
							synchronized(alive){
								alive = false;
								System.out.println("Socket disconnected.");
								break; //exit the loop if the socket isn't alive
							}
						} 
					}
					//read message from server
					synchronized(in){
						try {
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
						} catch (IOException e) {
							System.out.println("Could not read ainput from socket. Killing socket.");
							synchronized(alive){
								alive = false;
							}
							return;
						}
					}
				}
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
			alive = false;
		}
		try {
			synchronized(socket){
				socket.close();
			}
		} catch (IOException e) {
			System.out.println("Could not close socket.");
			e.printStackTrace();
		}
	}
	
	/**
	 * String getHostname(void):
	 * Returns the hostname the socket is connected to.
	 * @return - the hostname.
	 */
	public String getHostname(){
		return hostname;
	}
	
	/**
	 * Integer getPort(void):
	 * Returns the port the socket is connected to.
	 * @return - the port.
	 */
	public Integer getPort(){
		return port;
	}
}
