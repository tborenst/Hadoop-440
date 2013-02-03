package testing;

import networking.*;

public class Testing {
	
	private Object lock = new Object();
	
	public void A(){
		synchronized(lock){
			System.out.println("We are in A!");
			B();
		}
	}
	
	public void B(){
		synchronized(lock){
			System.out.println("We are in B!");
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException{
		Testing test = new Testing();
		test.A();
	}
}
