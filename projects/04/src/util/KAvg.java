package util;
/*
 * Implement KAvg for your own running averager to work with your own KData.
 * By Vansi Vallabhaneni
 */



import java.io.Serializable;

public interface KAvg extends Serializable {
	/**
	 * Generate the average.
	 * @return
	 */
	public KData getAverage();
	
	/**
	 * Add a data point to your running average calculation.
	 * @param d
	 */
	public void addDataPt(KData d);
	
	/**
	 * Clears your running average.
	 */
	public void clear();
}
