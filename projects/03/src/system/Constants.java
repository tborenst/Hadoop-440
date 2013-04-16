package system;

public class Constants {
	// task types
	public static String MAP = "MAP";
	public static String SORT = "SORT";
	public static String REDUCE = "REDUCE";
	// task statuses
	public static String READY = "READY";
	public static String PENDING = "PENDING"; // job status
	public static String COMPLETED = "COMPLETED"; // job status
	// job status
	public static String MAPPING = "MAPPING";
	public static String SORTING = "SORTING";
	public static String REDUCING = "REDUCING";
	public static String FAILED = "FAILED";
	public static String STOPPED = "STOPPED";
	// SIO event names
	public static String TASK_COMPLETE = "TASK_COMPLETE";
	public static String TASK_REQUEST = "TASK_REQUEST";
	public static String TASK_ERROR = "TASK_ERROR";
	public static String JOB_FAILED = "JOB_FAILED";     // TODO: on: get id of failed job, tell them to debug 
	public static String JOB_REQUEST = "JOB_REQUEST";
	public static String JOB_STATUS = "JOB_STATUS";     // TODO: emit: send job id, on: get string of status
	public static String JOB_ID = "JOB_ID";             // TODO: on: get int of job id
	public static String JOB_COMPLETE = "JOB_COMPLETE"; // TODO: on: get int of job id, tell them job complete
	public static String STOP_JOB = "STOP_JOB";         // TODO: emit: send int of job id
	public static String START_JOB = "START_JOB";       // TODO: emit: send int of job id
}
