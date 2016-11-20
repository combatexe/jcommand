package org.jcommand.queue.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jcommand.queue.api.QueueObject;
import org.prevayler.SureTransactionWithQuery;

final class PollQueueObject<T extends QueueObject> implements SureTransactionWithQuery<PersistenceQueue<T>, List<T>> {
	private static final long serialVersionUID = 1L;
	private Integer poolSize;

	public PollQueueObject(Integer poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	public List<T> executeAndQuery(PersistenceQueue<T> persistenceQueue, Date date) {

		List<T> pollResultList = new ArrayList<>(poolSize);
		T queueObject;
		for (int count = 0; poolSize > count; count++) {
			queueObject = persistenceQueue.queue.poll();
			if (queueObject != null) {
				persistenceQueue.inTransaction.put(queueObject.getId(), queueObject);
				pollResultList.add(queueObject);
			}
		}
		return pollResultList;
	}
}