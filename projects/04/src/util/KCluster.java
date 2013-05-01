package util;

import java.io.Serializable;
import java.util.ArrayList;

public class KCluster<T> implements Serializable {
	private static final long serialVersionUID = 3950930551187321015L;
	
	private ArrayList<T> data;
	private T centroid;
	
	public KCluster(T centroid) {
		this.centroid = centroid;
		this.data = new ArrayList<T>();
	}
	
	public KCluster(T centroid, ArrayList<T> data) {
		this.centroid = centroid;
		this.data = data;
	}
	
	public ArrayList<T> getData() {
		return data;
	}
	
	public void setData(ArrayList<T> newData) {
		data = newData;
	}
	
	public void addDataPt(T dataPt) {
		data.add(dataPt);
	}
	
	public T getCentroid() {
		return centroid;
	}
	
	public void setCentroid(T newCentroid) {
		centroid = newCentroid;
	}
	
	public static void main(String[] args) {
		ArrayList<Integer> d = new ArrayList<Integer>();
		d.add(1);
		d.add(2);
		d.add(3);
		d.add(4);
		
		KCluster<Integer> c = new KCluster<Integer>(2);
		c.addDataPt(1);
		
	}
}
