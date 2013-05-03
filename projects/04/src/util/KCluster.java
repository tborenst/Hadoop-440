/*
 * By Vansi Vallabhaneni
 */

package util;

import java.io.Serializable;
import java.util.ArrayList;

public class KCluster implements Serializable {
	private static final long serialVersionUID = 3950930551187321015L;	
	private ArrayList<KData> data;
	private KData centroid;
	private KAvg runningAvg;
	
	public KCluster(KData centroid, KAvg runningAvg) {
		this.centroid = centroid;
		this.data = new ArrayList<KData>();
		this.runningAvg = runningAvg;
	}
	
	public KCluster(KData centroid, ArrayList<KData> data, KAvg runningAvg) {
		this.centroid = centroid;
		this.runningAvg = runningAvg;
		setData(data);
	}
	
	/**
	 * Calculate the running average of this cluster.
	 * @return
	 */
	public KData getAverage() {
		return runningAvg.getAverage();
	}
	
	/**
	 * Getter for this cluster's data.
	 * @return
	 */
	public ArrayList<KData> getData() {
		return data;
	}
	
	/**
	 * Setter for this cluster's data.
	 * @param newData
	 */
	public void setData(ArrayList<KData> newData) {
		data = new ArrayList<KData>();
		runningAvg.clear();		
		
		for(int d = 0; d < newData.size(); d++) {
			addDataPt(newData.get(d));
		}
		
	}
	
	/**
	 * Add a data point to this cluster.
	 * @param dataPt
	 */
	public void addDataPt(KData dataPt) {
		data.add(dataPt);
		runningAvg.addDataPt(dataPt);
	}
	
	/**
	 * Getter for this cluster's centroid.
	 * @return
	 */
	public KData getCentroid() {
		return centroid;
	}
	
	/**
	 * Setter for this cluster's centroid.
	 * @param newCentroid
	 */
	public void setCentroid(KData newCentroid) {
		centroid = newCentroid;
	}
	
	/**
	 * For Debugging.
	 */
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
