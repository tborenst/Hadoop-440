package parallel;

import mpi.*;
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
		

		this.ctr = 0;
		this.numProcs = numProcs;
		this.masterRank = masterRank;
		this.masterSlave = new KMeansSlave(this.masterRank, this.masterRank, this.numProcs);
		
		Random rgenerator = new Random();
		for(int i = 0; i < k; i++) {
			KData centroid = dataset.get(rgenerator.nextInt(dataset.size()));
			KCluster cluster = new KCluster(centroid, (KAvg) KAvgClass.getConstructor().newInstance());
			this.clusters.add(cluster);
		}
		
		this.clusterDataset();
				
		while(!this.withinRange(centroidEpsilon)) {
			this.findNewClusters();
			this.clusterDataset();
			this.ctr++;
		}
		
		killSlaves();
	}
	
	private void killSlaves() {
		KMessage[] messages = new KMessage[numProcs];
		sendMessages(messages);
	}
	
	/**
	 * Send messages to the slaves using gather and scatter.
	 * @param messages
	 */
	private void sendMessages(KMessage[] messages) {
		MPI.COMM_WORLD.Scatter(messages, 0, 1, MPI.OBJECT, messages, 0, 1, MPI.OBJECT, masterRank);
		masterSlave.handleMessage(messages[0]);
		MPI.COMM_WORLD.Gather(messages, 0, 1, MPI.OBJECT, messages, 0, 1, MPI.OBJECT, masterRank);
	}
	
	/**
	 * Check to see if KMeansMaster is complete based off the distance between the old centroids and the current centroids.
	 * @param centroidEpsilon
	 * @return
	 * @throws Throwable 
	 */
	private boolean withinRange(double centroidEpsilon) throws Throwable {
		if(clusters.size() <= 0) {
			throw new Throwable("KMeansMaster: there was a fatal error, for somereason all the clusters died.");
		}
		
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
		sendMessages(clusterWork);
		
		// merge clusters
		ArrayList<KCluster> baseClusters = clusterWork[0].getClusters();
		
		for(int m = 1; m < clusterWork.length; m++) {
			ArrayList<KCluster> toMergeClusters = clusterWork[m].getClusters();
			for(int c = 0; c < baseClusters.size(); c++) {
				KCluster baseCluster = baseClusters.get(c);
				baseCluster.mergeWith(toMergeClusters.get(c));
			}
		}
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
