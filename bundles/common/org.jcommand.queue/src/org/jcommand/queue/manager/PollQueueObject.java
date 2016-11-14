package org.jcommand.queue.manager;

import java.util.Date;

import org.prevayler.SureTransactionWithQuery;

final class PollQueueObject<T> implements SureTransactionWithQuery<PersistenceQueue<T>, T> {
	private static final long serialVersionUID = 1L;

	@Override
	public T executeAndQuery(PersistenceQueue<T> persistenceQueue, Date date) {
		return persistenceQueue.poll();
	}
}