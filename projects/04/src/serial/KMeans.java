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
	
	public KMeans(ArrayList<KData> dataset, int k, Class<?> KAvgClass) throws Throwable {
		// TODO: throw error if k <= 0
		
		if(!Util.classImplements(KAvgClass, KAvg.class)) {
			throw new Throwable("KAvgClass must implement util.KAvg.");
		}
		
		this.dataset = dataset;
		
		//empty clusters
		this.clusters = new ArrayList<KCluster>();
		Random rgenerator = new Random();
		
		for(int i = 0; i < k; i++) {
			KData centroid = dataset.get(rgenerator.nextInt(dataset.size()));
			KCluster cluster = new KCluster(centroid, (KAvg) KAvgClass.getConstructor().newInstance());
			this.clusters.add(cluster);
		}
		
		this.clusterDataset();
		
		while(this.withinRange()) {
			this.findNewClusters();
			this.clusterDataset();
		}
	}
	
	private boolean withinRange() {
		for(int c = 0; c < clusters.size(); c++) {
			KCluster cluster = clusters.get(c);
			if(!cluster.distancesWithin(10)) {
				return false;
			}
		}
		
		return true;
	}
	
	private void findNewClusters() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		ArrayList<KCluster> newClusters = new ArrayList<KCluster>();
		for(int c = 0; c < clusters.size(); c++) {
			// TODO: catch reflections errors and throw a better exception
			newClusters.add(new KCluster(clusters.get(c).getAverage(), (KAvg) KAvgClass.getConstructor().newInstance()));
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
		int minDist = dataPt.distanceTo(closestCluster.getCentroid());
		for(int c = 0; c < clusters.size(); c++) {
			KCluster cluster = clusters.get(c);
			int distance = dataPt.distanceTo(cluster.getCentroid());
			if(minDist > distance) {
				minDist = distance;
				closestCluster = cluster;
			}
		}
		
		return closestCluster;
	}
	
		
	
}
