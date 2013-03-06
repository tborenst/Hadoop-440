/**
 * The Example2 class demonstrates how to use our RMI system through example of usage.
 * In particular, this example demonstrates "pass-by-reference", bind/re-bind/un-bind features.
 * @author Tomer Borenstein
 */
package examples;

import rmi.ClientHandler;
import rmi.ServerHandler;

public class Example2 {
	
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
		rmiServer.registerClass(RemoteCounterFactoryImpl.class, RemoteCounterFactory.class); 
		rmiServer.registerClass(RemoteCounterImpl.class, RemoteCounter.class);
		//register new objects as remote services
		RemoteCounterFactory factory = new RemoteCounterFactoryImpl();
		rmiServer.registerObject(factory, RemoteCounterFactory.class, "counterFactory");
		
		//============//
		//setup client//
		//============//
		ClientHandler rmiClient1 = new ClientHandler();
		ClientHandler rmiClient2 = new ClientHandler();
		//register interfaces on the client
		rmiClient1.registerInterface(RemoteCounterFactory.class);
		rmiClient1.registerInterface(RemoteCounter.class);
		rmiClient2.registerInterface(RemoteCounterFactory.class);
		rmiClient2.registerInterface(RemoteCounter.class);
		
		//================//
		//application code//
		//================//
		rmiClient1.connectTo(hostname, port);
		rmiClient2.connectTo(hostname, port);
		
		//get factory and requests a remote counter
		System.out.println("Test Pass By Reference...");
		RemoteCounterFactory remFactory1 = (RemoteCounterFactory) rmiClient1.lookup("counterFactory");
		RemoteCounter countBy1One = remFactory1.makeRemoteCounter(1);
		
		//test countBy1 counter
		System.out.println("Count By 1 (One): " + countBy1One.getCount());
		countBy1One.stepCount();
		System.out.println("Count By 1 (One): " + countBy1One.getCount());
		countBy1One.stepCount();
		System.out.println("Count By 1 (One): " + countBy1One.getCount());
		
		//register countBy1 as a service by binding it
		System.out.println("Test bind() feature...");
		rmiClient1.bind("countBy1", countBy1One.getROR());
		
		//have another client request countBy1
		RemoteCounter countBy1Two = (RemoteCounter) rmiClient2.lookup("countBy1");
		System.out.println("Count By 1 (Two): " + countBy1Two.getCount());
		//test that countBy1Two affects countBy1One
		countBy1Two.stepCount();
		System.out.println("Count By 1 (Two): " + countBy1Two.getCount());
		System.out.println("Count By 1 (One): " + countBy1One.getCount());
		
		//have the client2 re-bind the "countBy1" service
		System.out.println("Test rebind() feature...");
		RemoteCounterFactory remFactory2 = (RemoteCounterFactory) rmiClient2.lookup("counterFactory");
		RemoteCounter countBy2Two = remFactory2.makeRemoteCounter(2);
		
		//test countBy2 counter
		System.out.println("Count By 2 (Two): " + countBy2Two.getCount());
		countBy2Two.stepCount();
		System.out.println("Count By 2 (Two): " + countBy2Two.getCount());
		countBy2Two.stepCount();
		System.out.println("Count By 2 (Two): " + countBy2Two.getCount());
		
		//re-bind countBy2 counter to "countBy1" service and see changes reflect in client1
		rmiClient2.rebind("countBy1", countBy2Two.getROR());
		countBy1One = (RemoteCounter) rmiClient1.lookup("countBy1");
		System.out.println("Count By 1 (One): " + countBy1One.getCount());
		countBy1One.stepCount();
		System.out.println("Count By 1 (One): " + countBy1One.getCount());
		countBy1One.stepCount();
		System.out.println("Count By 1 (One): " + countBy1One.getCount());
		
		//have client1 un-bind the service (it's angry at client2 for re-biding it...)
		System.out.println("Test unbind() feature...");
		rmiClient1.unbind("countBy1");
		try{
			countBy2Two = (RemoteCounter) rmiClient2.lookup("countBy1");
		} catch (Exception e){
			System.out.println("Error! Could not find service.");
		}
	}
}
