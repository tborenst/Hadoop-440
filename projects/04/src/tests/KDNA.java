package tests;

import util.KData;

public class KDNA implements KData{
	
	private String[] datapoint;
	
	public KDNA(String[] datapoint){
		this.datapoint = datapoint;
	}
	
	public String[] getDatapoint(){
		return datapoint;
	}
	
	public double distanceTo(KData d) {
		String[] datapoint1 = datapoint;
		String[] datapoint2 = ((KDNA)d).getDatapoint();
		
		// throw error if datapoints do not have the same length
		if(datapoint1.length != datapoint2.length){
			(new Exception("KDNA points do not have the same length.")).printStackTrace();
		}
		
		float distance = 0;
		
		for(int i = 0; i < datapoint1.length; i++){
			String base1 = datapoint1[i];
			String base2 = datapoint2[i];
			
			if(!base1.equals(base2)){
				distance += 1.0;
			}
		}
		
		return distance;
	}
	
	public String toString(){
		String result = "[";
		
		for(int i = 0; i < datapoint.length; i++){
			if(i == datapoint.length - 1){
				result += (datapoint[i] + "]");
			} else {
				result += (datapoint[i] + ", ");
			}
		}
		
		return result;
	}
	
}
