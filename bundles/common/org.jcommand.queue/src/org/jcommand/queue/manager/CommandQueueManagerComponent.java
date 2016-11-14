package org.jcommand.queue.manager;

import java.util.concurrent.Executor;

import org.jcommand.api.Command;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "service.pid=org.jcommand.cm.persistencemanager.QueueConfiguration",
		"service.scope=singleton" })
public class CommandQueueManagerComponent extends QueueManagerComponent<Command> implements ManagedServiceFactory {

	@Reference
	public void bindExecutor(Executor executor) {
		super.executor = executor;
	}

	public void unbindExecutor(Executor executor) {
		super.executor = null;
	}
}
