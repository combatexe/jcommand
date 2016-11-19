package org.jcommand.queue.manager;

import java.util.Date;
import java.util.List;

import org.prevayler.SureTransactionWithQuery;

final class CommitQueueObject<T> implements SureTransactionWithQuery<PersistenceQueue<T>, Boolean> {
	private static final long serialVersionUID = 1L;
	private List<Long> queueObjectIds;

	public CommitQueueObject(List<Long> queueObjectIds) {
		this.queueObjectIds = queueObjectIds;
	}

	@Override
	public Boolean executeAndQuery(PersistenceQueue<T> persistenceQueue, Date date) {
		boolean isSuccsses = true;
		for (Long queueObjectId : queueObjectIds) {
			isSuccsses &= persistenceQueue.inTransaction.remove(queueObjectId) != null ? Boolean.TRUE : Boolean.FALSE;
		}
		return isSuccsses;
	}
}