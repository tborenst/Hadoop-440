package networking;

public abstract class SIOSocket {
	
	//send Object 'obj' with String 'message'
	public void emit(String event, Object obj){
	}
	
	//respond to a blocking request with id 'requestId', with an Object 'obj'
	public void respond(int requestId, Object obj){
	}
	
	//check whether this socket is alive
	public Boolean isAlive(){
		return null;
	}
}
