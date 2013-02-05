package migratableProcesses;

public class Timer implements MigratableProcess{
	private static final long serialVersionUID = -4277211513428020483L;
	private int time;
	private int counter;
	private boolean infinity;
	private volatile boolean suspending;
	
	public Timer(String[] args){
		this.time = Integer.parseInt(args[0]);
		if(args[1].equals("infinity")){
			this.infinity = true;
		} else {
			this.infinity = false;
		}
		this.counter = 0;
		this.suspending = false;
	}
	
	@Override
	public void run() {
		while(!suspending){
			if(!infinity && time <= counter){
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
