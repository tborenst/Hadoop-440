/**
 * SIOPacket encodes a String message and an Object object to be sent between client and server sockets.
 * It makes it easy to encode a message and an object in an SIOPacket, send the packet, and then have it
 * be unpacked on the other side.
 * If the packet is sent by the client to the server, and is set to be blocking, the server will know that
 * it must respond to this particular request.
 */
package networking;

import java.io.Serializable;
public class SIOPacket implements Serializable{
	private static final long serialVersionUID = 4619069636246433999L;
	private String message;
	private Object object;
	private Boolean blocking;
	private int requestId;
	
	public SIOPacket(String message, Object object){
		this.message = message;
		if(!(object instanceof java.io.Serializable) && object != null){
			Warning.Warn("object in SIOPacket must be serializable");
			this.object = null;
		} else {
			this.object = object;
		}
		this.blocking = false; //packets are non-blocking by default
		this.requestId = -1;
	}
	
	public String getMessage(){
		return message;
	}
	
	public Object getObject(){
		return object;
	}
	
	public void setBlocking(Boolean bool){
		blocking = bool;
	}
	
	public Boolean isBlocking(){
		return blocking;
	}
	
	public void setRequestId(int id){
		requestId = id;
	}
	
	public int getRequestId(){
		return requestId;
	}
}
