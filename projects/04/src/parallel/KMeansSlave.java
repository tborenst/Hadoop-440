package parallel;

import util.KCluster;
import util.KData;

public class KMeansSlave {
	private int rank;
	private masterRank;
	private int numProcs;
	
	public KMeansSlave(int rank, int masterRank, int numProcs) {
		this.rank = rank;
		this.masterRank = masterRank;
		this.numProcs = numProcs;
	}
	
	public startListening() {
		
	}
	
	public listenForWork() {
		
	}
	
	public void clusterDataset(ArrayList<KCluster> clusters, ArrayList<KData> dataset) {
		for(int d = 0; d < dataset.size(); d++) {
			KData dataPt = dataset.get(d);
			KCluster closestCluster = findClosestCluster(dataPt);
			closestCluster.addDataPt(dataPt);
		}
	}
	
	/**
	 * Find the closest cluster based off the data point.
	 * @param dataPt
	 * @return
	 */
	private KCluster findClosestCluster(ArrayList<KClusters> clusters, KData dataPt) {
		KCluster closestCluster = clusters.get(0);
		double minDist = dataPt.distanceTo(closestCluster.getCentroid());
		for(int c = 0; c < clusters.size(); c++) {
			KCluster cluster = clusters.get(c);
			double distance = dataPt.distanceTo(cluster.getCentroid());
			if(minDist > distance) {
				minDist = distance;
				closestCluster = cluster;
			}
		}
		
		return closestCluster;
	}
}
