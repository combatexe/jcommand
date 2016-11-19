package org.jcommand.queue.manager;

public enum QueueStatus {
	UNDEFINE(1), ERROR(1 << 1), STOPPING(1 << 2), STOP(1 << 3), SUSPEND(1 << 4), STARTING(1 << 5), RUNNING(
			1 << 6), CREATING(1 << 7), EXIST(1 << 8), DELETING(1 << 9), DELETE(1 << 10), MAXENTRIES(1 << 11);

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
		if (statusCode == null || statusCode <= 0 || statusCode >= Math.pow(2, values().length - 1)) {
			return UNDEFINE;
		}
		int position = Integer.numberOfTrailingZeros(statusCode);
		return values()[position];
	};

	public static String toString(int intStatus) {

		StringBuilder statusAsString = new StringBuilder(100);

		statusAsString.append("QueueStatus : ");

		for (QueueStatus status : values()) {
			statusAsString.append('[');
			statusAsString.append(status.name());
			statusAsString.append('=');
			if ((intStatus & status.statusCode) == status.statusCode) {
				statusAsString.append(1);
			} else {
				statusAsString.append(0);
			}
			statusAsString.append(']');
			statusAsString.append(':');
		}
		statusAsString.deleteCharAt(statusAsString.length() - 1);
		return statusAsString.toString();
	}
}
