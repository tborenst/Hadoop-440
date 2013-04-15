package vansitest;

import java.io.Console;
import java.io.FileNotFoundException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import system.Request;
import system.InValidConfigFileException;

public class ClientTest {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, FileNotFoundException, InValidConfigFileException {
		 Console c = System.console();
	    if (c != null) {
	        // printf-like arguments
	        c.format("asdf >");
	        c.format("\nPress ENTER to proceed.\n");
	        c.readLine();
	    }
		
		
		//Request r = Request.constructFromFile("C:/Users/vansi/Documents/School/15440/projects/03/src/vansitest/config.json");
		//System.out.println(r.getMapperDirectory());
		//r.exportTo("C:/Users/vansi/Documents/School/15440/projects/03/src/vansitest/configExp.json");

		
		
		/*final CommandPrompt c = new CommandPrompt();
		c.on("poop", new SIOCommand() {
			
			public void run() {
				c.emit("===> poop land <===");
			}
		});
		
		c.on("tomer", new SIOCommand() {
			
			public void run() {
				c.emit("===> tomer land <===");
			}
		});
		
		c.on("quit", new SIOCommand() {
			
			public void run() {
				c.emit("Closing system...");
				System.exit(0);
			}
		});*/
	}
}
