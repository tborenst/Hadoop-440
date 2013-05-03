import mpi.*;

class MPIHelloWorld {
   private static String s = "meowmeow";
 
    public static int main(String[] args) {
        int rank, size;
        
        try {

        MPI.Init(args);
        rank = MPI.COMM_WORLD.Rank();
        size = MPI.COMM_WORLD.Size();

        if (rank == 0) {
            char[] m = "Hello, there".toCharArray();
            MPI.COMM_WORLD.Send(m, 0, m.length, MPI.CHAR, 1, 99);
            System.out.println("Process " + rank + " out of " + size + s);
        } else {
            char[] m = new char[20];
            MPI.COMM_WORLD.Recv(m, 0, 20, MPI.CHAR, 0, 99);
            System.out.println("received: " + new String(m));

            System.out.println("Process " + rank + " out of " + size + s);

        }

        MPI.Finalize();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}
