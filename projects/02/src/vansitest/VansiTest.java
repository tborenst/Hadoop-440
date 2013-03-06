/**
 * Vansi Tests his code here.
 * Author: Vansi Vallabaneni.
 */

package vansitest;

import java.rmi.AlreadyBoundException;

import rmi.*;

public class VansiTest {

	public static void main(String[] args) throws AlreadyBoundException, NoSuchRemoteObjectReferenceException {
		
		
		//setup
		int serverPort = 8080;
		ServerHandler s = new ServerHandler(serverPort);
		String serverHostname = s.getHostname();
		s.registerClass(PersonImpl.class, Person.class);
		s.registerObject(new PersonImpl(1, "doom"), Person.class, "tomer");
		s.registerObject(new PersonImpl(1, "tanya"), Person.class, "adopt");
		
		
		ClientHandler c = new ClientHandler();
		c.connectTo(serverHostname, serverPort);
		c.registerInterface(Person.class);
		
		
		try {
			//actually start doing shit
			Person t = (Person) c.lookup("tomer");
			
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
//			t2.getChild(0);
			System.out.println("------------");
			RemoteObjectReference rorT2 = t2.getROR();
			System.out.println(">> "+rorT2);
			c.bind("child", rorT2);
			Person t2FromBind = (Person) c.lookup("child");
			System.out.println(t2FromBind.getName());
			//c.unbind("child");
			//c.lookup("child");
			c.rebind("child", t.getROR());
			
			Person tNotReallyChild = (Person) c.lookup("child");
			System.out.println(tNotReallyChild.getName());
			
			
			Person toAdopt = (Person) c.lookup("adopt");
			System.out.println("going to adopt: " + toAdopt.getName() + " " + (toAdopt instanceof Person));

			System.out.println("\n------------ ");
			t.adoptChild(toAdopt);
			Person toAdoptFromT = (Person) t.getChild(1);
			System.out.println("adopted: "+toAdoptFromT.getName());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
