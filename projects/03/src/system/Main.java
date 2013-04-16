package system;

import java.net.UnknownHostException;

public class Main {
	public static void main(String[] args) {
		if(args.length >= 3) {
			if(args[0].equals("-m") && args.length >= 4) {
				int slavePort = 0;
				int clientPort = 0;
				try {
					slavePort = Integer.parseInt(args[1]);
					clientPort = Integer.parseInt(args[2]);					
				} catch(NumberFormatException e) {
					System.out.println("To launch a master, run './mapReduce -m <slave port number> <client port number> <working directory>',\n" +
							"slave port and client port numbers must be integers.");
					return;
				}
				
				MasterRoutine m = new MasterRoutine(slavePort, clientPort, args[3]);
				
				try {
					String localhostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
					System.out.println("Created a slave port at: " + localhostname + ":" + slavePort);
					System.out.println("Created a client port at: " + localhostname + ":" + clientPort);
				} catch (UnknownHostException e1) {
					System.out.println("Unable to find server's IP Address, please try again.");
				}
				
				return;
				
			} else if (args[0].equals("-s")) {
				int slavePort = 0;
				String[] addr = args[1].split(":");
				try {
					slavePort = Integer.parseInt(addr[1]);
				} catch(NumberFormatException e) {
					System.out.println("To launch a slave, run './mapReduce -s <hostname>:<slave port number> <working directory>',\n" +
							"slave port number must be integers.");
					return;
				}
				
				SlaveRoutine s = new SlaveRoutine(addr[0], slavePort, args[2]);
				return;
			} else if(args[0].equals("-c")) {
				int clientPort = 0;
				String[] addr = args[1].split(":");
				try {
					clientPort = Integer.parseInt(addr[1]);
				} catch(NumberFormatException e) {
					System.out.println("To launch a slave, run './mapReduce -s <hostname>:<client port number> <working directory>',\n" +
							"client port number must be integers.");
					return;
				}
				
				ClientRoutine c = new ClientRoutine(addr[0], clientPort);
				return;
			}
		}
		
		System.out.println("ERROR: Malformed expression.");
		System.out.println("To launch a master, run './mapReduce -m <slave port number> <client port number> <working directory>'.");
		System.out.println("To launch a slave, run './mapReduce -s <hostname>:<slave port number> <working directory>'.");
		System.out.println("To launch a client, run './mapReduce -c <hostname>:<client port number> <working directory>'.");
		System.out.println("To launch a slave and a client on the same machine, launch the slave first and then the client.");
	}

}
