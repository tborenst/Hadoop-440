/**
 * Tomer tests his code here.
 */
package testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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
				try {
					ServerSocket server = new ServerSocket(15237);
					Socket incoming = server.accept();
					InputStream inStream = incoming.getInputStream();
					ObjectInputStream inObjStream = new ObjectInputStream(inStream);
					Object obj = inObjStream.readObject();
					System.out.println("--SERVER--");
					System.out.println("RECIEVED OBJECT:");
					System.out.println(obj);
					System.out.println("Cat Name: " + ((Cat)obj).getName());
					System.out.println("Cat Age:  " + ((Cat)obj).getAge());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
		}).start();
		
		new Thread(new Runnable(){
			// CLIENT THREAD
			
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					Socket client = new Socket("localhost", 15237);
					OutputStream outStream = client.getOutputStream();
					ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
					Cat missy = new Cat("Missy", 4);
					System.out.println("--CLIENT--");
					System.out.println("SENDING OBJECT...");
					objOutStream.writeObject(missy);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}).start();
	}
}
