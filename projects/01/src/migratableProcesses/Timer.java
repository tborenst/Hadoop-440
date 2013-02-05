package migratableProcesses;

public class Timer implements MigratableProcess{
	private static final long serialVersionUID = -4277211513428020483L;
	private int time;
	private int counter;
	private volatile boolean suspending;
	
	public Timer(String[] args){
		System.out.println("I AM THE CONSTRUCTOR!");
		this.time = Integer.parseInt(args[0]);
		this.counter = 0;
		this.suspending = false;
	}
	
	@Override
	public void run() {
		while(!suspending){
			if(time <= counter){
				return;
			} else {
				counter++;
			}
		}
		suspending = false;
	}
	
	@Override
	public void suspend() {
		suspending = true;
		while(suspending);
	}
}
