package serial;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import util.KAvg;
import util.KCluster;
import util.KData;
import util.Util;

import java.util.Random;

public class KMeans {
	private ArrayList<KData> dataset;
	private ArrayList<KCluster> clusters;
	private ArrayList<Double> centroidEpsilons;
	private Class<?> KAvgClass;
	private int ctr;
	
	public KMeans(ArrayList<KData> dataset, Class<?> KAvgClass, int k, double centroidEpsilon) throws Throwable {
		if(k <= 0) {
			throw new Throwable("KMeans: k must be greater than 0.");
		}
		
		this.dataset = dataset;

		if(!Util.classImplements(KAvgClass, KAvg.class)) {
			throw new Throwable("KMeans: KAvgClass must implement util.KAvg.");
		}
		
		this.KAvgClass = KAvgClass;
		
		// empty clusters
		this.clusters = new ArrayList<KCluster>();
		this.centroidEpsilons = new ArrayList<Double>();
		
		Random rgenerator = new Random();
		for(int i = 0; i < k; i++) {
			KData centroid = dataset.get(rgenerator.nextInt(dataset.size()));
			KCluster cluster = new KCluster(centroid, (KAvg) KAvgClass.getConstructor().newInstance());
			this.clusters.add(cluster);
		}
		
		this.clusterDataset();
		
		this.ctr = 0;
		while(!this.withinRange(centroidEpsilon)) {
			this.findNewClusters();
			this.clusterDataset();
			this.ctr++;
		}
	}
	
	/**
	 * Check to see if KMeans is complete based off the distance between the old centroids and the current centroids.
	 * @param centroidEpsilon
	 * @return
	 */
	private boolean withinRange(double centroidEpsilon) {
		//System.out.print("[");
		for(int i = 0; i < centroidEpsilons.size(); i++) {
			//System.out.print(centroidEpsilons.get(i) + ", ");
			if(centroidEpsilons.get(i) > centroidEpsilon) {
				//System.out.println("] term early");
				return false;
			}
		}
		//System.out.println("]");
		return centroidEpsilons.size() == clusters.size();
	}
	
	/**
	 * Create the new clusters (based off the average of the old clusters).
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private void findNewClusters() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		ArrayList<KCluster> newClusters = new ArrayList<KCluster>();
		centroidEpsilons = new ArrayList<Double>();
		
		for(int c = 0; c < clusters.size(); c++) {
			// TODO: catch reflections errors and throw a better exception
			KCluster cluster = clusters.get(c);
			KData avg = cluster.getAverage();
			
			// empty sets will have a null avg
			
			if(avg != null) {
				centroidEpsilons.add(avg.distanceTo(cluster.getCentroid()));
				KAvg newAverager = (KAvg) KAvgClass.getConstructor().newInstance();
				newClusters.add(new KCluster(avg, newAverager));
			}
		}
		clusters = newClusters;
	}
	
	/**
	 * Add the data to the closest cluster.
	 */
	private void clusterDataset() {
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

	/**
	 * For debugging.
	 */
	public String toString() {
		String result = "Created " + clusters.size() + " clusters in " + ctr + " iterations...\n";
		for(int c = 0; c < clusters.size(); c++) {
			result += "Cluster: " + c + "\n" + clusters.get(c).toString();
			result += "------------------\n";
		}
		
		return result;
	}
}
