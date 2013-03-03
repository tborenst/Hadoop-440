/**
 * Vansi Tests his code here.
 */

package vansitest;

import networking.SIOClient;
import rmi.*;

public class VansiTest {

	public static void main(String[] args) throws Exception {
		int serverPort = 8080;
		ServerHandler s = new ServerHandler(serverPort);
		String serverHostname = s.getHostname();
		s.addObject(new PersonImpl(1, "doom"), "Person", "tomer");
		
		ClientManager c = new ClientManager();
		SIOClient sock = c.connectTo(serverHostname, serverPort);
		c.addInterface(Person.class.getSimpleName(), Person.class);
		Person t = (Person) c.lookupOn(sock, "tomer");
		System.out.println("-------");
		System.out.println(t.getName());
		
	}
}
