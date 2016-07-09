package org.jcommand.queue.capability.service;

import java.util.Date;
import java.util.Set;

import org.prevayler.SureTransactionWithQuery;

public class QueryActiveQueueIds implements SureTransactionWithQuery<CapabilityRoot, Set<Long>> {

	private static final long serialVersionUID = 6028617412036805228L;

	@Override
	public Set<Long> executeAndQuery(CapabilityRoot root, Date arg1) {
		return root.repository.keySet();
	}

}
