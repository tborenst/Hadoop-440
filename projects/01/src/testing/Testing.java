package testing;

import java.util.Scanner;

import processManager.CommandPrompt;
import processManager.NodeManager;
import processManager.NodeProxy;
import processManager.SlaveNode;
//import processManager.NodeManager;
import networking.*;

public class Testing {
	
	public static void main(String[] args) throws InterruptedException{
//		NodeManager manager = new NodeManager(5, 5000);
//		System.out.println("aa");
//		Thread.sleep(1000);
//		final SIOClient client1 = new SIOClient("192.168.1.16", 4313);
//		client1.on("quit", new SIOCommand(){
//			public void run(){
//				System.out.println("OOOH NOOO!");
//				System.out.println("Client disconnected.");
//				client1.close();
//			}
//		});
//		Thread.sleep(1000);
//		manager.quit();
//		Thread.sleep(1000);
//		final SIOClient client2 = new SIOClient("192.168.1.16", 4313);
//		client2.on("quit", new SIOCommand(){
//			public void run(){
//				System.out.println("Oh well!");
//				client2.close();
//			}
//		});
//		Thread.sleep(1000);
//		manager.quit();
//		
//		final CommandPrompt prompt = new CommandPrompt();
//		new Thread(new Runnable(){
//			public void run(){
//				try {
//					Thread.sleep(5000);
//					prompt.emit("This message is brought to you by ProcessManager Inc,.");
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//		}).start();
//		prompt.on("ps", new SIOCommand(){
//			public void run(){
//				System.out.println("PS...");
//			}
//		});
//		prompt.on("quit", new SIOCommand(){
//			public void run(){
//				System.exit(0);
//			}
//		});
//		prompt.on("addNewProcess", new SIOCommand(){
//			public void run(){
//				System.out.println(args[0]);
//				System.out.println(args[1]);
//			}
//		});
		
//		NodeProxy n = new NodeProxy(237);
//		n.addNewProcess(23, "Grayer");
//		n.addNewProcess(12, "WebCrawl");
//		n.addNewProcess(49, "Saxpy.java");
//		n.addNewProcess(97, "arbit.ser");
//		n.addNewProcess(104, "tertarin");
//		System.out.println(n.getProcessesAsString());
//		System.out.println(n.getProcessesAsString());
		
		NodeManager master = new NodeManager(5, 5000, 4013);
		Thread.sleep(1000);
		//master.addNewProcess("migratableProcesses.Grayer", "[./images/flower.jpg,jpeg]");
		//master.ps();
		
	}
}
