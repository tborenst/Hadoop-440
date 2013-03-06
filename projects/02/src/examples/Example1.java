/**
 * The Example1 class demonstrates how to use our RMI system through example of usage.
 * In particular, this example demonstrates "pass-by-value", along with the basic usage of our system.
 * @author Tomer Borenstein
 */

package examples;

import rmi.ClientHandler;
import rmi.ServerHandler;

public class Example1 {

	public static void main(String[] args) throws Exception {
		
		//example setup
		int port = 8080;
		String hostname;
		
		//============//
		//setup server//
		//============//
		ServerHandler rmiServer = new ServerHandler(port);
		hostname = rmiServer.getHostname();
		//register classes and interfaces on the server
		rmiServer.registerClass(RemoteCalculatorImpl.class, RemoteCalculator.class);
		//register new objects as remote services
		RemoteCalculator calc = new RemoteCalculatorImpl();
		rmiServer.registerObject(calc, RemoteCalculator.class, "calc");
		
		//============//
		//setup client//
		//============//
		ClientHandler rmiClient = new ClientHandler();
		//register interfaces on the client
		rmiClient.registerInterface(RemoteCalculator.class);
		
		//================//
		//application code//
		//================//
		rmiClient.connectTo(hostname, port);
		
		RemoteCalculator remCalc = (RemoteCalculator) rmiClient.lookup("calc");
		System.out.println("5 + 5 = " + remCalc.add(5, 5));
		System.out.println("2 * 3 = " + remCalc.mult(2, 3));
		System.out.println("9 - 6 = " + remCalc.sub(9, 6));
		System.out.println("8 / 4 = " + remCalc.div(8, 4));
		
	}
}
