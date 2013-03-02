/**
 * SIOPacket encodes a String message and an Object object to be sent between client and server sockets.
 * It makes it easy to encode a message and an object in an SIOPacket, send the packet, and then have it
 * be unpacked on the other side.
 * If the packet is sent by the client to the server, and is set to be blocking, the server will know that
 * it must respond to this particular request.
 */
package networking;

public class SIOPacket {
	private String message;
	private Object object;
	private Boolean blocking;
	
	public SIOPacket(String message, Object object){
		this.message = message;
		this.object = object;
	}
	
	public String getMessage(){
		return message;
	}
	
	public Object getObject(){
		return object;
	}
}
