
import mpi.*;
import java.util.ArrayList;
import java.util.Random;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MPI.Init(args);
			int rank = MPI.COMM_WORLD.Rank();
			int procs = MPI.COMM_WORLD.Size();

			if(rank == 0) {
				System.out.println("Master started");
				ArrayList<KData> data = new ArrayList<KData>();
				
				int xRange = 100000;
				int yRange = 100000000;
				Random randGen = new Random();		
				for(int d = 0; d < 100; d++) {
					K2D dataPt = new K2D(randGen.nextInt(xRange) - xRange/2, randGen.nextInt(yRange) - yRange/2);
					data.add(dataPt);
				}
				
				
				KMeansMaster k = new KMeansMaster(data, K2DAvg.class, 2, 0.5);

			} else {
				System.out.println("Started slave " + rank);
			}

			MPI.Finalize();
		} catch(MPIException e) {
			System.out.println("MPI Exception");
			e.printStackTrace();
		}
	}
}
