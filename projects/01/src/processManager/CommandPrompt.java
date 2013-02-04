package processManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import networking.SIOCommand;

public class CommandPrompt {
	HashMap<String, SIOCommand> bindings;
	Object printLock;
	Boolean promptGiven;
	
	public CommandPrompt(){
		this.bindings = new HashMap<String, SIOCommand>();
		this.printLock = new Object();
		this.promptGiven = false;
		givePrompt();
	}
	
	/**
	 * void on(String, SIOCommand):
	 * Bind a certain SIOCommand to be run when a certain message is "received".
	 * @param message
	 * @param command
	 */
	public void on(String message, SIOCommand command){
		synchronized(bindings){
			bindings.put(message, command);
		}
	}
	
	/**
	 * void emit(String):
	 * Print a certain message to the user, then give them back the prompt.
	 * @param message
	 */
	public void emit(String message){
		synchronized(printLock){
			System.out.println(message);
			givePrompt();
		}
	}
	
	/**
	 * void givePrompt(void):
	 * Give the user a prompt. Wait for them to type something, then take the prompt away and analyze it.
	 */
	private void givePrompt(){
		synchronized(promptGiven){
			if(promptGiven == true){
				//someone else has the prompt
				return;
			} else {
				//we are giving the prompt away
				promptGiven = true;
			}
			
			Runnable prompt = new Runnable(){
				public void run(){
					Scanner scanner = new Scanner(System.in);
					System.out.print("> ");
					String input = scanner.nextLine();
					promptGiven = false;
					analayzeInput(input);
				}
			};
			
			new Thread(prompt).start();
		}
	}
	
	/**
	 * void analyzeInput(String):
	 * Analyzes given input and tries to verifies that the class actually exists. If the class doesn't exit, print an error message.
	 * Note that for processes to be executed, they must be present in the migratableProcesses package.
	 * @param input
	 */
	private void analayzeInput(String input){
		//analyze input
		String[] inputArray = input.split(" ");
		String className = "migratableProcesses." + inputArray[0];         //get process name
		//check if the class exist
		try {
			Class<?> process = Class.forName(className);
			System.out.println("Executing '" + className + "'...");
			synchronized(bindings){
				SIOCommand addNewProcess = bindings.get("addNewProcess");
				if(addNewProcess == null){
					System.out.println("Failed to execute process '" + className + "'.");
					givePrompt();
				} else {
					addNewProcess.parameters(inputArray);
					try{
						addNewProcess.run();
					} catch (Exception e){
						System.out.println("Failed to execute process '" + className + "'.");
						givePrompt();
					}
				}
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Process '" + className + "' not found.");
			givePrompt();
		}
	}
}
