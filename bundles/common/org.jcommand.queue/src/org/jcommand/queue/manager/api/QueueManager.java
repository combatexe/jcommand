package org.jcommand.queue.manager.api;

public interface QueueManager {

	void createQueue();
	
	void removeQueue();
	
	void suspendQueue();
	
	void resumQueue();
	
}
