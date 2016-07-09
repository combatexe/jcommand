package org.jcommand.queue.capability.service;

import java.util.Date;

import org.prevayler.SureTransactionWithQuery;

final class CreateNewDBId implements SureTransactionWithQuery<CapabilityRoot, Long> {
	private static final long serialVersionUID = 1L;

	@Override
	public Long executeAndQuery(CapabilityRoot root, Date arg1) {
		root.dbVersionSequencer += root.dbVersionSequencer;
		return root.dbVersionSequencer;
	}
}