/**
 * Tomer tests his code here.
 */
package tomertest;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import rmi.RemoteObjectReference;

import networking.SIOClient;
import networking.SIOCommand;
import networking.SIOServer;

public class TomerTest {
	
	public static class Cat implements Serializable{
		private String name;
		private int age;
		
		public Cat(String name, int age){
			this.name = name;
			this.age = age;
		}
		
		public String getName(){
			return name;
		}
		
		public int getAge(){
			return age;
		}
	}
	
	public static void main(String args[]) throws IOException{
		
		new Thread(new Runnable(){
			// SERVER THREAD
			
			@Override
			public void run() {
				SIOServer server = new SIOServer(15237);
				server.on("add", new SIOCommand(){
					public void run(){
						int[] array = (int[])object;
						RemoteObjectReference ror = new RemoteObjectReference(null, requestId, null, null);
						socket.respond(requestId, array[0]+array[1]);
						socket.respond(requestId, 0);
					}
				});
			}
			
		}).start();
		
		new Thread(new Runnable(){
			// CLIENT THREAD
		
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					SIOClient client = new SIOClient("localhost", 15237);
					int[] toadd = {2, 90};
					int five = (Integer)client.request("add", toadd);
					System.out.println(five);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			
		}).start();
	}
}
