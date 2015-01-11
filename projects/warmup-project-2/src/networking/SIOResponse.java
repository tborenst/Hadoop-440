/**
 * SIOResponse is a wrapper for a Boolean value and an Object. 
 * It encodes a response from the server to a blocking request from the client.
 */
package networking;

public class SIOResponse {
	private Boolean status;
	private Object object;
	
	public SIOResponse(Boolean status, Object object){
		this.status = status;
		this.object = object;
	}
	
	public void setStatus(Boolean bool){
		status = bool;
	}
	
	public void passObject(Object object){
		this.object = object;
	}
	
	public Boolean getStatus(){
		return status;
	}
	
	public Object getObject(){
		return object;
	}
}
