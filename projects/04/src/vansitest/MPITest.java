package vansitest;

import mpi.*;

public class MPITest {	
	public static int main(String[] args) {
    System.out.println("here");
    
    try {
    MPI.Init(args);
    int rank = MPI.COMM_WORLD.Rank();
    int procs = MPI.COMM_WORLD.Size();
    
    if(rank == 0) {
      System.out.println("Master started");
      
      int id = 0;
      int[] ids = new int[]{id};
      MPI.COMM_WORLD.Send(ids, 0, 1, MPI.INT, 1, 99);
      MPI.COMM_WORLD.Recv(ids, 0, 1, MPI.INT, MPI.ANY_SOURCE, 99);
      System.out.println("new id: " + id);
      
    } else {
      int[] ids = new int[]{};
      MPI.COMM_WORLD.Recv(ids, 0, 1, MPI.INT, MPI.ANY_SOURCE, 99);
      
      ids[0]++;
      System.out.println(rank + ": set id to: " + ids[0]);
      
      MPI.COMM_WORLD.Send(ids, 0, 1, MPI.INT, 0, 99);
    }
    
    MPI.Finalize();
    } catch(MPIException e) {
      System.out.println("MPI Exception");
      e.printStackTrace();
    }
    
    return 1;
	}
}
