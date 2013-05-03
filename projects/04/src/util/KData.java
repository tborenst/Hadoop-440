package util;
/*
 * Implement this interface for your own data point class.
 * By Vansi Vallabhaneni
 */



import java.io.Serializable;

public interface KData extends Serializable {
	/**
	 * Distance between this data point and the given data point.
	 * @param d
	 * @return
	 */
	public double distanceTo(KData d);
	
	/**
	 * Please write your own toString method, this will help you debug.
	 * @return
	 */
	public String toString();
}
