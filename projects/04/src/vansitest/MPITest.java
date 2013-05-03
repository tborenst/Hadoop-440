package vansitest;

import mpi.*;
import java.io.*;

public class MPITest {	

public static class Task implements Serializable {
	private static final long serialVersionUID = 8088351904681221275L;
	public int id;

	public Task(){
		this.id = 0;
	}
}	

public static void main(String[] args) {
    System.out.println("here");
    
    try {
    MPI.Init(args);
    int rank = MPI.COMM_WORLD.Rank();
    int procs = MPI.COMM_WORLD.Size();
    
    if(rank == 0) {
      System.out.println("Master started");
      
      Task id = new Task();
      Task[] ids = new Task[]{id};
      MPI.COMM_WORLD.Send(ids, 0, 1, MPI.OBJECT, 1, 99);
      MPI.COMM_WORLD.Recv(ids, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, 99);
      System.out.println("new id: " + ids[0].id);
      
    } else {
      Task[] ids = new Task[]{new Task()};
      MPI.COMM_WORLD.Recv(ids, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, 99);
      
      ids[0].id++;;
      System.out.println(rank + ": set id to: " + ids[0].id);
      MPI.COMM_WORLD.Send(ids, 0, 1, MPI.OBJECT, 0, 99);
    }
    
    MPI.Finalize();
    } catch(MPIException e) {
      System.out.println("MPI Exception");
      e.printStackTrace();
    }
    
	}
}
