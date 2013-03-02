package networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class SIOClient extends SIOSocket{
	private String hostname;
	private int port;
	private Socket socket;
	private Boolean alive;
	private ObjectInputStream objIn;
	private ObjectOutputStream objOut;
	private HashMap<String, SIOCommand> bindings;
	
	public SIOClient(String hostname, int port){
		this.hostname = hostname;
		this.port = port;
		this.bindings = new HashMap<String, SIOCommand>();
		try {
			this.socket = new Socket(hostname, port);
			this.objIn = new ObjectInputStream(socket.getInputStream());
			this.objOut = new ObjectOutputStream(socket.getOutputStream());
			this.alive = true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		listen();
	}
	
	/**
	 * Listen to messages from the server.
	 */
	private void listen(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(true){
					synchronized(objIn){
						try {
							//get and interpret an SIOPacket from server
							SIOPacket packet = (SIOPacket)objIn.readObject();
							String message = packet.getMessage();
							Object object = packet.getObject();
							synchronized(bindings){
								//attempt to run corresponding SIOCommand
								SIOCommand command = bindings.get(message);
								if(command != null){
									command.passObject(object);
									try{
										command.run();
									} catch (Exception e){
										e.printStackTrace();
									}
								}
							}
						} catch (IOException e) {
							//socket disconnected
							disconnect();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}
	
	/**
	 * Send a String 'message' with Object 'object' to server, and wait for a response from
	 * the server. This function is blocking, and returns the object that comes with the
	 * corresponding response from the server.
	 * @param message - message to the server
	 * @param object - object to the server
	 * @return the object that comes with the corresponding 
	 */
	public Object request(String message, Object object){
		SIOPacket packet = new SIOPacket(message, object);
		return null;
	}
	
	/**
	 * Disconnect socket and call "disconnect" SIOCommand.
	 */
	private void disconnect(){
		synchronized(alive){
			alive = false;
		}
		synchronized(socket){
			//close the socket
			try{
				socket.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		synchronized(bindings){
			//attempt to run SIOCommand corresponding to "disconnect"
			SIOCommand command = bindings.get("disconnect");
			if(command != null){
				command.passObject(null); //no object for disconnect
				try{
					command.run();
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}

}
