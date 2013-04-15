package api;

public class JobStatus {
	private int jobId;
	private String status;
	
	public JobStatus(int jobId, String status) {
		this.jobId = jobId;
		this.status = status;
	}

	public int getJobId() {
		return jobId;
	}
	
	public String getStatus() {
		return status;
	}
}
