package testing;

import processManager.SlaveNode;

public class Testing2 {
	public static void main(String[] args){
		SlaveNode slave = new SlaveNode("localhost", 4013, "./serialization/");
	}
}
