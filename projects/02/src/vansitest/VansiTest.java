/**
 * Vansi Tests his code here.
 */

package vansitest;

import rmi.ClientHandler;
import rmi.ServerHandler;

public class VansiTest {

	public static void main(String[] args) {
		ServerHandler s = new ServerHandler(8080);
		s.RMIIndex.add(new PersonImpl(1, "tomer"));
		
		ClientHandler c = new ClientHandler();
		//c.
	}
}
