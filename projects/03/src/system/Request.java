/**
 * Represents all data required for a new map reduce request.
 * Reads and writes JSON.
 * Requires Jackson JSON parser (http://jackson.codehaus.org/)
 * The library is included in MapReduce/libs/jackson-all-1.9.11.jar, please make sure to add it to you build path.
 */
package system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import fileio.UnableToAccessFileException;

import system.InValidConfigFileException;
import util.Util;

public class Request implements Serializable{
	private static final long serialVersionUID = 891493633097482745L;
	private static String nullConstant = "null"; //the user can do this to choose not to specify an optional field
	
	public class Mapper implements Serializable{
		private static final long serialVersionUID = -7393316563049538774L;
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

		public boolean isValid() {
			return numMappers > 0
					&& directory != null && Util.isValidDirectory(directory)
					&& fileName != null && (new File(directory + "/" + fileName)).exists()
					&& binaryName != null;
		}
	}
	
	public class Combiner implements Serializable{
		private static final long serialVersionUID = -8988134777487218052L;
		private String directory;
		private String fileName;
		private String binaryName;
		
		public String getDirectory() {
			if(directory.toLowerCase().equals(nullConstant)) {
				return null;
			}
			
			return directory;
		}

		public String getFileName() {
			if(fileName.toLowerCase().equals(nullConstant)) {
				return null;
			}
			
			return fileName;
		}

		public String getBinaryName() {
			if(binaryName.toLowerCase().equals(nullConstant)) {
				return null;
			}
			
			return binaryName;
		}

		public boolean isValid() {
			return directory != null && (directory.toLowerCase().equals(nullConstant) || Util.isValidDirectory(directory))
					&& fileName != null && (fileName.toLowerCase().equals(nullConstant) || (new File(directory + "/" + fileName)).exists())
					&& binaryName != null;
		}
	}
	
	public class Reducer implements Serializable{
		private static final long serialVersionUID = 4776071571078605186L;
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

		public boolean isValid() {
			return numReducers > 0
					&& directory != null && Util.isValidDirectory(directory)
					&& fileName != null && (new File(directory + "/" + fileName)).exists()
					&& binaryName != null;
		}
	}
	
	// These are public as required by the JSON parsing library.
	// But please do not use them, use the getters instead.
	public Mapper Map;
	public Combiner Combine;
	public Reducer Reduce;	
	public String[] dataPaths;	
	public String resultsDirectory;
	
	public Request() {}
	
	public static Request constructFromFile(String configFilePath) throws JsonParseException, JsonMappingException, InValidConfigFileException, FileNotFoundException, UnableToAccessFileException {
		ObjectMapper m = new ObjectMapper();
		File f = new File(configFilePath);
		Request req = null;
		if(f.exists()) {
				try {
					req = m.readValue(f, Request.class);
				} catch (IOException e) {
					e.printStackTrace();
					throw new UnableToAccessFileException();
				}
			if(!req.isValid()) {
				throw new InValidConfigFileException();
			}
		} else {
			throw new FileNotFoundException();
		}
		
		return req;
	}
	
	/**
	 * Exports the Request object in to a file in valid JSON.
	 * @param exportPath
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws FileNotFoundException - if file not found or unable to access file
	 */
	public void exportTo(String exportPath) throws JsonGenerationException, JsonMappingException, FileNotFoundException {
		ObjectMapper m = new ObjectMapper();
		

		try {
			File f = new File(exportPath);
			if(!f.exists()) {
				f.createNewFile();
			}
			m.writeValue(f, this);
			
		} catch (IOException e) {
			throw new FileNotFoundException();
		}
	}
	
	
	private boolean isValid() {
		
		return Map != null && Map.isValid() 
				&& Combine != null && Combine.isValid()
				&& Reduce != null && Reduce.isValid() 
				&& dataPaths != null && dataPaths.length > 0
				&& Util.isValidDirectory(resultsDirectory);
	}

	//--------
	// Getters
	// -------
	
	// Map
	public int getNumMappers() {
		return Map.getNumMappers();
	}
	
	public String getMapperDirectory() {
		return Map.getDirectory();
	}

	public String getMapperFileName() {
		return Map.getFileName();
	}

	public String getMapperBinaryName() {
		return Map.getBinaryName();
	}
	
	// Combine
	public String getCombinerDirectory() {
		return Combine.getDirectory();
	}

	public String getCombinerFileName() {
		return Combine.getFileName();
	}

	public String getCombinerBinaryName() {
		return Combine.getBinaryName();
	}
	
	// Reduce
	public int getNumReducers() {
		return Reduce.getNumReducers();
	}
	
	public String getReducerDirectory() {
		return Reduce.getDirectory();
	}

	public String getReducerFileName() {
		return Reduce.getFileName();
	}

	public String getReducerBinaryName() {
		return Reduce.getBinaryName();
	}
	
	public String[] getDataPaths() {
		return dataPaths;
	}
	
	public String getResultsDirectory() {
		return resultsDirectory;
	}
}
