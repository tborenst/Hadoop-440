package parallel;

import mpi.*;
import java.util.ArrayList;

import util.KCluster;
import util.KData;

public class KMeansSlave {
	private int rank;
	private int masterRank;
	private int numProcs;
	private boolean loop;
	
	public KMeansSlave(int rank, int masterRank, int numProcs) {
		this.rank = rank;
		this.masterRank = masterRank;
		this.numProcs = numProcs;
		this.loop = false;
	}
	
	public void startListening() {
		loop = true;
		while(loop) {
			listenForWork();
		}
	}
	
	public void stopListening() {
		loop = false;
	}
	
	public void listenForWork() {
		KMessage[] messages = new KMessage[1];
		MPI.COMM_WORLD.Scatter(messages, 0, 1, MPI.OBJECT, messages, 0, 1, MPI.OBJECT, masterRank);
		handleMessage(messages[0]);
		MPI.COMM_WORLD.Gather(messages, 0, 1, MPI.OBJECT, messages, 0, 1, MPI.OBJECT, masterRank);
		
	}
	
	public void handleMessage(KMessage message) {
		// TODO: change to a better stop message
		if(message == null) {
			stopListening();
		} else {
			ArrayList<KCluster> clusters = message.getClusters();
			ArrayList<KData> dataset = message.getData();
				
			clusterDataset(clusters, dataset);
			message.setClusters(clusters);
		}
	}
	
	private void clusterDataset(ArrayList<KCluster> clusters, ArrayList<KData> dataset) {
		for(int d = 0; d < dataset.size(); d++) {
			KData dataPt = dataset.get(d);
			KCluster closestCluster = findClosestCluster(clusters, dataPt);
			closestCluster.addDataPt(dataPt);
		}
	}
	
	/**
	 * Find the closest cluster based off the data point.
	 * @param dataPt
	 * @return
	 */
	private KCluster findClosestCluster(ArrayList<KCluster> clusters, KData dataPt) {
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
