package org.jcommand.queue.manager;

public enum QueueStatus {
	ERROR(-1), UNDEFINE(0), STOPPING(1), STOP(1 << 1), SUSPEND(1 << 2), STARTING(1 << 3), RUNNING(2 << 4), CREATING(
			1 << 5), EXIST(1 << 6), DELETING(1 << 7), DELETE(1 << 8);

	public final int statusCode;

	private QueueStatus(int statusCode) {
		this.statusCode = statusCode;
	}

	public static boolean hasStatus(QueueStatus toCheck, QueueStatus... statusList) {
		return hasStatus(toCheck.statusCode, statusList);
	}

	public static boolean hasStatus(int toCheck, QueueStatus... statusList) {
		int checkStatus = 0;
		for (QueueStatus queueStatus : statusList) {
			checkStatus |= queueStatus.statusCode;
		}
		return (toCheck & checkStatus) != 0;
	}

	public static QueueStatus fromStatusCode(Integer statusCode) {
		if (statusCode == null || statusCode <= 0 || statusCode > Math.pow(2, values().length)) {
			return UNDEFINE;
		}
		double position = Math.sqrt(statusCode);
		return position % 1 == 0 ? values()[(int) position + 2] : UNDEFINE;
	};
}
