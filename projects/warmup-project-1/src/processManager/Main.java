package processManager;

public class Main {
	public static void main(String[] args){
		if(args.length == 0){
			//master node
			NodeManager master = new NodeManager(1, 5000, 4013);
		} else if(args[0].equals("-c")) {
			//slave node
			String hostname = args[1];
			SlaveNode node = new SlaveNode(hostname, 4013, "./serialization/", 5000);
		} else {
			//wrong input
			System.out.println("Usage: [no arguments] OR [-c <hostname>]");
		}
	}
}
