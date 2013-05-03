package parallel;

import java.io.Serializable;
import java.util.ArrayList;

import util.KCluster;
import util.KData;

public class KMessage implements Serializable {
	private static final long serialVersionUID = 5940307369288309875L;
	private ArrayList<KCluster> clusters;
	private ArrayList<KData> dataset;
	
	public KMessage(ArrayList<KCluster> clusters, ArrayList<KData> dataset) {
		this.setClusters(clusters);
		this.setData(dataset);
	}

	public ArrayList<KCluster> getClusters() {
		return clusters;
	}

	public void setClusters(ArrayList<KCluster> newClusters) {
		clusters = newClusters;
	}

	public ArrayList<KData> getData() {
		return dataset;
	}

	public void setData(ArrayList<KData> newDataset) {
		dataset = newDataset;
	}
	
	
}
