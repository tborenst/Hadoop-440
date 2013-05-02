package serial;

import java.lang.reflect.*;
import java.util.ArrayList;

import util.KAvg;
import util.KCluster;
import util.KData;
import util.Util;

import java.util.Random;

public class KMeans {
	private ArrayList<KData> dataset;
	private ArrayList<KCluster> clusters;
	private Class<?> KAvgClass;
	private int ctr;
	
	public KMeans(ArrayList<KData> dataset, Class<?> KAvgClass, int k, double minDistance) throws Throwable {
		// TODO: throw error if k <= 0
		if(k <= 0) {
			throw new Throwable("KMeans: k must be greater than 0.");
		}
		
		this.dataset = dataset;

		if(!Util.classImplements(KAvgClass, KAvg.class)) {
			throw new Throwable("KMeans: KAvgClass must implement util.KAvg.");
		}
		
		this.KAvgClass = KAvgClass;
		
		//empty clusters
		this.clusters = new ArrayList<KCluster>();
		Random rgenerator = new Random();
		
		for(int i = 0; i < k; i++) {
			KData centroid = dataset.get(rgenerator.nextInt(dataset.size()));
			KCluster cluster = new KCluster(centroid, (KAvg) KAvgClass.getConstructor().newInstance());
			this.clusters.add(cluster);
		}
		
		this.clusterDataset();
		
		this.ctr = 0;
		while(!this.withinRange(minDistance)) {
			ctr++;
			this.findNewClusters();
			this.clusterDataset();
		}
	}
	
	private boolean withinRange(double minDistance) {
		for(int c = 0; c < clusters.size(); c++) {
			KCluster cluster = clusters.get(c);
			if(!cluster.distancesWithin(minDistance)) {
				return false;
			}
		}
		
		return true;
	}
	
	private void findNewClusters() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		ArrayList<KCluster> newClusters = new ArrayList<KCluster>();
		for(int c = 0; c < clusters.size(); c++) {
			// TODO: catch reflections errors and throw a better exception
			KData avg = clusters.get(c).getAverage();
			
			// empty sets will have a null avg
			
			if(avg != null) {
				KAvg newAverager = (KAvg) KAvgClass.getConstructor().newInstance();
				newClusters.add(new KCluster(avg, newAverager));
			}
		}
		clusters = newClusters;
	}
	
	private void clusterDataset(){
		for(int d = 0; d < dataset.size(); d++) {
			KData dataPt = dataset.get(d);
			KCluster closestCluster = findClosestCluster(dataPt);
			closestCluster.addDataPt(dataPt);
		}
	}
	
	private KCluster findClosestCluster(KData dataPt) {
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

	// for debugging
	public String toString() {
		String result = "Created " + clusters.size() + " clusters in " + ctr + " iterations...\n";
		for(int c = 0; c < clusters.size(); c++) {
			result += "Cluster: " + c + "\n" + clusters.get(c).toString();
			result += "------------------\n";
		}
		
		return result;
	}
	
}
