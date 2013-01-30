/**
 * The SocketIOMessage class allows SokcetIO sockets to bundle strings within an object that is easily transferred over the wire.
 */
package networking;

import java.io.Serializable;

public class SocketIOMessage implements Serializable{
	private String message;
	
	public SocketIOMessage(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}
