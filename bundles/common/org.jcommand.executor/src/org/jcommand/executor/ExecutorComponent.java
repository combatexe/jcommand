package org.jcommand.executor;

import java.util.Dictionary;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(property = { "service.pid=org.jcommand.cm.persistencemanager.ExecutorConfiguration",
		"service.scope=singleton" }, service = ManagedService.class)
public class ExecutorComponent implements Executor, ManagedService {

	private ExecutorService executor;

	private Integer poolTargetSize;
	private Integer maxWaitMinutesForShutdown;

	private BundleContext context;

	private ServiceRegistration<Executor> registerService;

	@Activate
	public void activate(BundleContext context) {
		this.context = context;
	}

	@Deactivate
	public void deactivate() throws InterruptedException {
		registerService.unregister();
		executor.shutdown();
		executor.awaitTermination(maxWaitMinutesForShutdown.longValue(), TimeUnit.MINUTES);
		for (Runnable runnable : executor.shutdownNow()) {
			// TODO FH: logging
			runnable.toString();
		}
	}

	@Override
	public void execute(Runnable command) {
		executor.execute(command);
	}

	@Override
	public void updated(Dictionary<String, ?> configuration) throws ConfigurationException {
		poolTargetSize = (Integer) configuration.get(ExecutorConfigurationKeys.poolTargetSize.getKey());
		maxWaitMinutesForShutdown = (Integer) configuration
				.get(ExecutorConfigurationKeys.maxWaitMinutesForShutdown.getKey());
		executor = Executors.newWorkStealingPool(poolTargetSize);
		registerService = context.registerService(Executor.class, executor, null);
	}

}
