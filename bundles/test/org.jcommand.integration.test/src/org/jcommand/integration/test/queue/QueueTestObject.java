package org.jcommand.integration.test.queue;

import org.jcommand.queue.api.QueueObject;

public class QueueTestObject implements QueueObject {

	private static final long serialVersionUID = 1L;

	@Override
	public Long getId() {
		return 1l;
	}

}
