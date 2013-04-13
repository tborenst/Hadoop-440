package system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import system.InValidConfigFileException;
import util.Util;

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

		public boolean isValid() {
			return numMappers > 0
					&& directory != null && Util.isValidDirectory(directory)
					&& fileName != null && (new File(directory + "/" + fileName)).exists()
					&& binaryName != null;
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

		public boolean isValid() {
			return directory != null && Util.isValidDirectory(directory)
					&& fileName != null && (new File(directory + "/" + fileName)).exists()
					&& binaryName != null;
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

		public boolean isValid() {
			return numReducers > 0
					&& directory != null && Util.isValidDirectory(directory)
					&& fileName != null && (new File(directory + "/" + fileName)).exists()
					&& binaryName != null;
		}
	}
	
	public Mapper Map;
	public Combiner Combine;
	public Reducer Reduce;	
	public String[] dataPaths;	
	public String resultsDirectory;
	
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
	
	private boolean isValid() {
		
		return Map != null && Map.isValid() 
				&& Combine != null && Combine.isValid()
				&& Reduce != null && Reduce.isValid() 
				&& dataPaths != null && dataPaths.length > 0
				&& Util.isValidDirectory(resultsDirectory);
	}

	// Map Data Getters
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
	
	// Combine Data Getters
	public String getCombinerDirectory() {
		return Combine.getDirectory();
	}

	public String getCombinerFileName() {
		return Combine.getFileName();
	}

	public String getCombinerBinaryName() {
		return Combine.getBinaryName();
	}
	
	// Reduce Data Getter
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
