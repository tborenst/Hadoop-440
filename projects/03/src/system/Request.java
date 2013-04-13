package system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import system.InValidConfigFileException;

public class Request {
	public class Mapper {
		private int numMappers;
		private String directory;
		private String fileName;
		private String binaryName;
		
		public int getNumMappers() {
			return numMappers;
		}

		public String getDirectory() {
			return directory;
		}

		public String getFileName() {
			return fileName;
		}

		public String getBinaryName() {
			return binaryName;
		}
	}
	
	public class Combiner {
		private String directory;
		private String fileName;
		private String binaryName;
		
		public String getDirectory() {
			return directory;
		}

		public String getFileName() {
			return fileName;
		}

		public String getBinaryName() {
			return binaryName;
		}
	}
	
	public class Reducer {
		private int numReducers;
		private String directory;
		private String fileName;
		private String binaryName;
		
		public int getNumReducers() {
			return numReducers;
		}

		public String getDirectory() {
			return directory;
		}

		public String getFileName() {
			return fileName;
		}

		public String getBinaryName() {
			return binaryName;
		}
	}
	
	public Mapper Map;
	public Combiner Combine;
	public Reducer Reduce;	
	public String[] dataPaths;	
	public String[] resultPaths;
	
	public Request() {}
	
	public static Request constructFromFile(String configFilePath) throws JsonParseException, JsonMappingException, InValidConfigFileException, FileNotFoundException {
		ObjectMapper m = new ObjectMapper();
		File f = new File(configFilePath);
		Request req = null;
		if(f.exists()) {
			try {
				req = m.readValue(f, Request.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!req.isValid()) {
				throw new InValidConfigFileException();
			}
		} else {
			throw new FileNotFoundException();
		}
		
		return req;
	}
	
	private boolean isValid() {
		return Reduce.getNumReducers() == resultPaths.length;
	}

	public void exportTo(String exportPath) {
		ObjectMapper m = new ObjectMapper();
		
		try {
			File f = new File(exportPath);
			if(!f.exists()) {
				f.createNewFile();
			}
			m.writeValue(f, this);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
