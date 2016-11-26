package org.jcommand.queue.manager;

import java.util.concurrent.Executor;

import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

@Component(property = { "service.pid=org.jcommand.cm.persistencemanager.QueueConfiguration",
		"service.scope=singleton" })
public class CommandQueueManagerComponent extends QueueManagerComponent<CommandQueueObject>
		implements ManagedServiceFactory {

	@Reference
	public void bindExecutor(Executor executor) {
		super.executor = executor;
	}

	public void unbindExecutor(Executor executor) {
		super.executor = null;
	}

	@Reference
	public void bindLogService(LogService logService) {
		super.logService = logService;
	}

}
