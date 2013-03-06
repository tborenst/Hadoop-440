/**
 * The Example3 class demonstrates how to use our RMI system through example of usage.
 * In particular, this example demonstrates passing errors from remote objects on the server to the client.
 * @author Tomer Borenstein
 */

package examples;

import rmi.ClientHandler;
import rmi.ServerHandler;

public class Example3 {
	
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
		try{
			System.out.println("Testing error passing...");
			System.out.println("5 / 0 = ...");
			remCalc.div(5, 0);
		} catch (ArithmeticException e){
			e.printStackTrace();
		}
	}
}
