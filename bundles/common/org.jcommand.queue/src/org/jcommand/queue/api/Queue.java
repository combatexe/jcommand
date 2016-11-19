package org.jcommand.queue.api;

import java.util.List;

public interface Queue<T extends QueueObject> {

	// process methods

	boolean offer(T command);

	T poll();

	boolean commit(Long commandId);

	List<T> poll(int pollSize);

	boolean commit(List<Long> commandIds);

	// management methods

	long getLoad();

	long getAvGCommandLiveQueue();

	long aggregateAvGExecuteTimeOfAllQueuedCommands();

}
