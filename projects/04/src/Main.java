
import mpi.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;

import parallel.KMeansMaster;
import parallel.KMeansSlave;

import util.*;
import tests.*;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable {
		try {
			MPI.Init(args);
			int rank = MPI.COMM_WORLD.Rank();
			int procs = MPI.COMM_WORLD.Size();
			int masterRank = 0;

			if(rank == masterRank) {
				System.out.println("Master started");
				ArrayList<KData> data = new ArrayList<KData>();
				
				int xRange = 100;
				int yRange = 100;
				Random randGen = new Random();		
				for(int d = 0; d < 10000000; d++) {
					//K2D dataPt = new K2D(randGen.nextInt(xRange) - xRange/2, randGen.nextInt(yRange) - yRange/2);
					KDNA dataPt = new KDNA(DNAGenerator.generateDNA(7));
          data.add(dataPt);
				}
				
				Date date1 = new Date();
        long start = date1.getTime();
        
				KMeansMaster k = new KMeansMaster(data, /*K2DAvg.class*/ KDNAAvg.class, 100, 0, masterRank, procs);
        
        Date date2 = new Date();
        long end   = date2.getTime();
        System.out.println("Duration: " + (end-start));
    
        //System.out.println(k.toString());

			} else {
				System.out.println("Started slave " + rank);
				KMeansSlave k = new KMeansSlave(rank, masterRank, procs);
				k.startListening();
			}

			MPI.Finalize();
		} catch(MPIException e) {
			System.out.println("MPI Exception");
			e.printStackTrace();
		}
	}
}
