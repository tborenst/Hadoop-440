package serial;

import java.util.ArrayList;
import util.KCluster;
import java.util.Random;

public abstract class AbstractKMeans<T> {
	private ArrayList<T> dataset;
	private ArrayList<KCluster<T>> clusters;
	
	public AbstractKMeans(ArrayList<T> dataset, int k){
		// TODO: throw error if k <= 0
		
		this.dataset = dataset;
		
		//empty clusters
		this.clusters = new ArrayList<KCluster<T>>();
		Random rgenerator = new Random();
		
		for(int i = 0; i < k; i++){
			T centroid = dataset.get(rgenerator.nextInt(dataset.size()));
			KCluster<T> cluster = new KCluster<T>(centroid);
			this.clusters.add(cluster);
		}
	}
	
	private void clusterDataset(){
		for(int d = 0; d < dataset.size(); d++) {
			T dataPt = dataset.get(d);
			KCluster<T> closestCluster = findClosestCluster(dataPt);
			closestCluster.addDataPt(dataPt);
		}
	}
	
	private KCluster<T> findClosestCluster(T dataPt) {
		KCluster<T> closestCluster = clusters.get(0);
		for(int c = 0; c < clusters.size(); c++) {
			KCluster<T> cluster = clusters.get(c);
			if(distance(dataPt, closestCluster.getCentroid()) > distance(dataPt, cluster.getCentroid())) {
				closestCluster = cluster;
			}
		}
		return closestCluster;
	}
	
	abstract int distance(T p1, T p2);
	
	
	
	
	
}
