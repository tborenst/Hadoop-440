/**
 * CommandPrompt object to interface with a human.
 */

package system;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import networking.SIOCommand;

public class CommandPrompt {
	private HashMap<String, SIOCommand> bindings;
	private Object printLock;
	private Boolean promptGiven;
	
	/**
	 * Constructor for CommandPrompt.
	 */
	public CommandPrompt() {
		this.bindings = new HashMap<String, SIOCommand>();
		this.printLock = new Object();
		this.promptGiven = false;
		givePrompt();
		System.out.print("> ");
	}
	
	/**
	 * Bind a certain SIOCommand to be run when a certain message is "received".
	 * @param message
	 * @param command
	 */
	public void on(String message, SIOCommand command) {
		synchronized(bindings) {
			bindings.put(message, command);
		}
	}
	
	/**
	 * Print a certain message to the user, then give them back the prompt.
	 * @param message
	 */
	public void emit(String message) {
		synchronized(printLock) {
			System.out.print("\n");
			System.out.println(message);
			givePrompt();
			System.out.print("> ");
		}
	}
	
	/**
	 * Give the user a prompt. Wait for them to type something, then take the prompt away and analyze it.
	 */
	private void givePrompt() {
		synchronized(promptGiven) {
			if(promptGiven == true) {
				//someone else has the prompt
				return;
			} else {
				//we are giving the prompt away
				promptGiven = true;
			}
			
			Runnable prompt = new Runnable() {
				@Override
				public void run(){
					Scanner scanner = new Scanner(System.in);
					String input = scanner.nextLine();
					promptGiven = false;
					analayzeInput(input);
				}
			};
			
			new Thread(prompt).start();
		}
	}
	
	/**
	 * Analyzes given input and tries to verifies that the class actually exists. If the class doesn't exit, print an error message.
	 * Note that for processes to be executed, they must be present in the migratableProcesses package.
	 * @param input
	 */
	private void analayzeInput(String input) {
		String[] inputArray = input.split(" ");
		String arg1 = inputArray[0];
		
		synchronized(bindings) {
			SIOCommand cmd = bindings.get(arg1);
			if(cmd != null) {
				String[] argsArray = Arrays.copyOfRange(inputArray, 1, inputArray.length);
				cmd.passObject(argsArray);
				try {
					cmd.run();
				} catch (Exception e) {
					emit("Failed to execute '" + input + "'.\n"
							+ "Due to excetion:" + e);
				}
			} else if(arg1.equals("")) {
				emit("");
			} else {
				emit("Command '" + arg1 + "' Not Found.");
			}
		}
	}
}
