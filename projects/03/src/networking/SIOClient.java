package networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;

public class SIOClient extends SIOSocket{
	private String hostname;
	private int port;
	private Socket socket;
	private Boolean alive;
	private ObjectInputStream objIn;
	private ObjectOutputStream objOut;
	private HashMap<String, SIOCommand> bindings;
	private HashMap<Integer, SIOResponse> requests;
	private int requestCount;
	
	public SIOClient(String hostname, int port){
		this.hostname = hostname;
		this.port = port;
		this.bindings = new HashMap<String, SIOCommand>();
		this.requests = new HashMap<Integer, SIOResponse>();
		this.requestCount = 0;
		try {
			this.socket = new Socket(hostname, port);
			this.objOut = new ObjectOutputStream(socket.getOutputStream());
			this.objIn = new ObjectInputStream(socket.getInputStream());
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
		final SIOSocket self = this;
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
							
							if(packet.isBlocking()){
								//this is a response to a blocking request
								int requestId = packet.getRequestId();
								synchronized(requests){
									requests.put(requestId, new SIOResponse(true, object));
								}
							} else {	
								//this is a normal non-blocking message from the server
								synchronized(bindings){
									//attempt to run corresponding SIOCommand
									SIOCommand command = bindings.get(message);
									if(command != null){
										command.passObject(object);
										command.passSocket(self);
										try{
											command.run();
										} catch (Exception e){
											e.printStackTrace();
										}
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
	 * Bind a particular String 'message' with an SIOCommand 'command'.
	 */
	public void on(String message, SIOCommand command){
		synchronized(bindings){
			bindings.put(message, command);
		}
	}
	
	/**
	 * Send a String 'message' with an Object 'object' as a non-blocking call to the server.
	 */
	@Override
	public void emit(String message, Object object){
		SIOPacket packet = new SIOPacket(message, object);
		sendPacket(packet);
	}
	
	/**
	 * Send a String 'message' with Object 'object' to server, and wait for a response from
	 * the server. This function is blocking, and returns the object that comes with the
	 * corresponding response from the server. If a request times out, this function returns null.
	 * @param message - message to the server
	 * @param object - object to the server
	 * @return the object that comes with the corresponding 
	 */
	public Object request(String message, Object object){
		long timeout = 100000; //timeout for request
		long requestTime;
		SIOPacket packet = new SIOPacket(message, object);
		packet.setBlocking(true); //this is a blocking server call
		int requestId;
		synchronized(requests){
			//add new request
			requestId = requestCount++;
			requests.put(requestId, new SIOResponse(false, null));
			//send packet
			packet.setRequestId(requestId);
			sendPacket(packet);
			requestTime = (new Date()).getTime();
		}
		//block while waiting for response
		while(true){
			synchronized(requests){
				SIOResponse response = requests.get(requestId);
				if(response.getStatus() == true){
					return response.getObject();
				} else {
					long currentTime = (new Date()).getTime();
					long duration = currentTime - requestTime;
					if(timeout < duration){
						//request timed out
						requests.remove(requestId);
						return null;
					}
				}
			}
		}
	}
	
	/**
	 * Send SIOPacket to server.
	 */
	private void sendPacket(SIOPacket packet){
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
	
	@Override
	public Boolean isAlive(){
		synchronized(alive){
			return alive;
		}
	}

}
