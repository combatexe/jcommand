package org.jcommand.queue.manager;

import java.util.Date;

import org.prevayler.SureTransactionWithQuery;

final class OfferQueueObject<T> implements SureTransactionWithQuery<PersistenceQueue<T>, Boolean> {
	private static final long serialVersionUID = 1L;
	private T queueObject;

	public OfferQueueObject(T queueObject) {
		this.queueObject = queueObject;
	}

	@Override
	public Boolean executeAndQuery(PersistenceQueue<T> persistenceQueue, Date date) {
		return persistenceQueue.queue.offer(queueObject);
	}
}