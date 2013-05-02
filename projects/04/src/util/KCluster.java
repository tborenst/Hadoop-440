package util;

import java.io.Serializable;
import java.util.ArrayList;

public class KCluster implements Serializable {
	private static final long serialVersionUID = 3950930551187321015L;
	
	private ArrayList<KData> data;
	private KData centroid;
	private double maxDistance;
	private KAvg runningAvg;
	
	public KCluster(KData centroid, KAvg runningAvg) {
		this.centroid = centroid;
		this.data = new ArrayList<KData>();
		this.runningAvg = runningAvg;
		this.maxDistance = -1;
	}
	
	public KCluster(KData centroid, ArrayList<KData> data, KAvg runningAvg) {
		this.centroid = centroid;
		this.runningAvg = runningAvg;
		setData(data);
	}
	
	public KData getAverage() {
		return runningAvg.getAverage();
	}
	
	public boolean distancesWithin(double targetDistance) {
		return maxDistance <= targetDistance;
	}
	
	public ArrayList<KData> getData() {
		return data;
	}
	
	public void setData(ArrayList<KData> newData) {
		data = new ArrayList<KData>();
		runningAvg.clear();
		maxDistance = -1;
		
		
		for(int d = 0; d < newData.size(); d++) {
			addDataPt(newData.get(d));
		}
		
	}
	
	public void addDataPt(KData dataPt) {
		data.add(dataPt);
		runningAvg.addDataPt(dataPt);
		
		double distance = centroid.distanceTo(dataPt);
		if(maxDistance == -1 || maxDistance < distance) {
			maxDistance = distance;
		}
	}
	
	public KData getCentroid() {
		return centroid;
	}
	
	public void setCentroid(KData newCentroid) {
		centroid = newCentroid;
	}
	
	public String toString() {
		String result = "Centered around: " + centroid.toString() + "\n";
		
		KData avg = runningAvg.getAverage();
		String avgString = "null";
		if(avg != null) {avgString = avg.toString();}
		
		result += "Avg: " + avgString + "\n";
		
		for(int d = 0; d < data.size(); d++) {
			result += d + ": " + data.get(d).toString() + "\n";
		}
		
		return result;
	}
	
}
