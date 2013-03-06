package examples;

import rmi.MyRemote;
import rmi.RemoteObjectReference;

public interface RemoteCalculator extends MyRemote{
	public int add(int a, int b);  //a + b
	public int mult(int a, int b); //a * b
	public int sub(int a, int b);  //a - b
	public int div(int a, int b);  //a / b
}
