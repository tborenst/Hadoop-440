package parallel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import util.KAvg;
import util.KCluster;
import util.KData;
import util.Util;

import java.util.Random;

public class KMeansMaster {
	private ArrayList<KData> dataset;
	private ArrayList<KCluster> clusters;
	private ArrayList<Double> centroidEpsilons;
	private Class<?> KAvgClass;
	private int ctr;
	private int numProcs;
	private int masterRank;
	private KMeansSlave masterSlave;
	
	public KMeansMaster(ArrayList<KData> dataset, Class<?> KAvgClass, int k, double centroidEpsilon, int masterRank, int numProcs) throws Throwable {
		if(k <= 0) {
			throw new Throwable("KMeansMaster: k must be greater than 0.");
		}
		
		this.dataset = dataset;

		if(!Util.classImplements(KAvgClass, KAvg.class)) {
			throw new Throwable("KMeansMaster: KAvgClass must implement util.KAvg.");
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
		this.numProcs = numProcs;
		this.masterRank = masterRank;
		
		while(!this.withinRange(centroidEpsilon)) {
			this.findNewClusters();
			this.clusterDataset();
			this.ctr++;
		}
	}
	
	/**
	 * Check to see if KMeansMaster is complete based off the distance between the old centroids and the current centroids.
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
		KMessage[] clusterWork = generateClusterWork();
		
		
		
	}
	
	private KMessage[] generateClusterWork() {
		ArrayList<ArrayList<KData>> dataPartitions = partitionDataset(numProcs);
		KMessage[] work = new KMessage[numProcs];
		
		for(int i = 0; i < dataPartitions.size(); i++) {
			work[i] = new KMessage(clusters, dataPartitions.get(i));
		}
		
		return work;
	}
	
	private ArrayList<ArrayList<KData>> partitionDataset(int numPartitions) {
		int datasetSize = dataset.size();
		int partitionSize = (datasetSize + numPartitions - 1)/numPartitions;
		ArrayList<ArrayList<KData>> partitions = new ArrayList<ArrayList<KData>>();
		
		for(int p = 0; p < datasetSize; p += partitionSize) {
			int endPt = p + partitionSize;
			if(endPt >= datasetSize) {endPt = datasetSize - 1;}
			partitions.add((ArrayList<KData>) dataset.subList(p, endPt));
		}
		
		return partitions;
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
