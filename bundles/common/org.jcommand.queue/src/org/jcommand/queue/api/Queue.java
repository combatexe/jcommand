package org.jcommand.queue.api;

public interface Queue<T> {

	// process methods

	boolean offer(T command);

	T poll();

	// management methods

	long getLoad();

	long getAvGCommandLivQueue();

	long aggregateAvGExecuteTimeOfAllQueuedCommands();

}
