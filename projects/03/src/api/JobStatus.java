package api;

import java.io.Serializable;

public class JobStatus implements Serializable{
	private static final long serialVersionUID = 616915574120416835L;
	
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
