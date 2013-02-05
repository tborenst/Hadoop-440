package processManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import util.Util;

import networking.SIOCommand;

public class CommandPrompt {
	private HashMap<String, SIOCommand> bindings;
	private Object printLock;
	private Boolean promptGiven;
	
	public CommandPrompt(){
		this.bindings = new HashMap<String, SIOCommand>();
		this.printLock = new Object();
		this.promptGiven = false;
		givePrompt();
		System.out.print("> ");
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
			System.out.print("\n");
			System.out.println(message);
			givePrompt();
			System.out.print("> ");
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
		String arg1 = inputArray[0];
		//ps, quit, or a process name?
		if(arg1.equals("ps")){
			ps();
			return;
		} else if(arg1.equals("quit")){
			quit();
			return;
		}
		String className = "migratableProcesses." + inputArray[0]; //get process name
		String[] argsArray = Arrays.copyOfRange(inputArray, 1, inputArray.length);
		//check if the class exist
		try {
			Class.forName(className); //see if you can find a class of name className
			emit("Executing '" + className + "'...");
			synchronized(bindings){
				SIOCommand addNewProcess = bindings.get("addNewProcess");
				if(addNewProcess == null){
					emit("Failed to execute process '" + className + "'.");
				} else {
					//set up parameters
					String[] parameters = {className, Util.stringifyArray(argsArray)};
					addNewProcess.parameters(parameters);
					try{
						addNewProcess.run();
					} catch (Exception e){
						emit("Failed to execute process '" + className + "'.");
					}
				}
			}
		} catch (ClassNotFoundException e) {
			emit("Process '" + className + "' not found.");
		}
	}
	
	/**
	 * void ps(void):
	 * Runs the SIOCommand associated with the String "ps".
	 */
	private void ps(){
		synchronized(bindings){
			SIOCommand ps = bindings.get("ps");
			try{
				ps.run();
			} catch(Exception e) {
				emit("Could not retrieve list of processes.");
			}
		}
	}
	
	/**
	 * void quit(void):
	 * Runs the SIOCommand associated with the String "quit".
	 */
	private void quit(){
		synchronized(bindings){
			SIOCommand quit = bindings.get("quit");
			try{
				quit.run();
			} catch (Exception e){
				emit("Could not quit ProcessManager.");
			}
		}
	}
}
