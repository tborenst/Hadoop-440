/**
 * Vansi Tests his code here.
 */

package vansitest;

import networking.SIOClient;
import rmi.*;

public class VansiTest {

	public static void main(String[] args) throws Exception {
		
		//setup
		int serverPort = 8080;
		ServerHandler s = new ServerHandler(serverPort, MyRemote.class);
		String serverHostname = s.getHostname();
		s.registerClass(PersonImpl.class, Person.class.getSimpleName());
		s.registerObject(new PersonImpl(1, "doom"), Person.class.getSimpleName(), "tomer");
		
		
		ClientManager c = new ClientManager();
		SIOClient sock = c.connectTo(serverHostname, serverPort);
		c.addInterface(Person.class.getSimpleName(), Person.class);
		
		
		
		//actually start doing shit
		Person t = (Person) c.lookupOn(sock, "tomer");
		
		
		System.out.println("-------");
		System.out.println(">> " + t.getName());
		t.setName("doom_rectum");
		System.out.println(">> " + t.getName());
		
		System.out.println(">> " + t.getAge());
		t.setAge(32142);
		System.out.println(">> " + t.getAge());
		
		Person t2 = (Person) t.makeChild("toby");
		System.out.println(">> " + t2.getName());
		t2.setName("doom_rectum2");
		System.out.println(">> " + t2.getName());
		
		System.out.println(">> " + t2.getAge());
		t2.setAge(2);
		System.out.println(">> " + t2.getAge());
		
		Person t2Copy = t.getChild(0);
		System.out.println(">> " + t2Copy.getName());
		t2Copy.setName("trollFace");
		System.out.println(">> "+t2.getName());
	}
}
