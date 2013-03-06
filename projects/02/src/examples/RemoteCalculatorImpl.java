package examples;

import rmi.RemoteObjectReference;

public class RemoteCalculatorImpl implements RemoteCalculator{
	private static final long serialVersionUID = 2033013265329658365L;

	public RemoteObjectReference getROR() {
		return null;
	}

	public int add(int a, int b) {
		return a+b;
	}

	public int mult(int a, int b) {
		return a*b;
	}

	public int sub(int a, int b) {
		return a-b;
	}
	
	public int div(int a, int b){
		return a/b;
	}

}
