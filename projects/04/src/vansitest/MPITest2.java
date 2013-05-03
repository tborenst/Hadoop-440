
import mpi.*;
import java.io.*;

public class MPITest2 {	

public static class Task implements Serializable {
	private static final long serialVersionUID = 8088351904681221275L;
	public int id;

	public Task(){
		this.id = 0;
	}
}	

public static void main(String[] args) {
    try {
    MPI.Init(args);
    int rank = MPI.COMM_WORLD.Rank();
    int procs = MPI.COMM_WORLD.Size();
    
    if(rank == 0) {
      System.out.println("Master started");
      
      Task[] ids = new Task[5];
      Task[] rIds = new Task[5];
      
      
      System.out.print("old ids: [");
      for(int i = 0; i < ids.length; i++) {
        ids[i] = new Task();
        System.out.print(ids[i].id + ", ");
      }
      System.out.println("]");
      
      MPI.COMM_WORLD.Scatter(ids, 0, 1, MPI.OBJECT, rIds, 0, 1, MPI.OBJECT, 0);
      
      MPI.COMM_WORLD.Gather(rIds, 0, 1, MPI.OBJECT, ids, 0, 1, MPI.OBJECT, 0);
      System.out.print("new ids: [");
      for(int i = 0; i < ids.length; i++) {
        System.out.print(ids[i].id + ", ");
      }
      
      System.out.println("]");
      
    } else {
      System.out.println("Slave " + rank + " started.");
      Task[] ids = new Task[5];
      Task[] rIds = new Task[5];
      for(int i = 0; i < rIds.length; i++) {
        ids[i] = new Task();
      }
      
      MPI.COMM_WORLD.Scatter(ids, 0, 1, MPI.OBJECT, rIds, 0, 1, MPI.OBJECT, 0);
      
      System.out.println("Recieved something.");
      
      rIds[rank-1].id++;
      System.out.println(rank + ": set id to: " + rIds[rank-1].id);
      
      MPI.COMM_WORLD.Gather(rIds, 0, 1, MPI.OBJECT, ids, 0, 1, MPI.OBJECT, 0);
      System.out.println("Slave Gather sent.");
    }
    
    MPI.Finalize();
    } catch(MPIException e) {
      System.out.println("MPI Exception");
      e.printStackTrace();
    }
    
	}
}
