package org.jcommand.queue.manager;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.jcommand.queue.api.QueueObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.log.LogService;

public abstract class QueueManagerComponent<T extends QueueObject> implements ManagedServiceFactory {

	private Map<String, QueueComponent<T>> existingServices = new HashMap<>();

	private BundleContext bundleContext;
	protected Executor executor;
	protected LogService logService;

	@Activate
	public void activate(BundleContext bundleContext) {
		logService.log(LogService.LOG_INFO, "activate " + getClass().getName());
		this.bundleContext = bundleContext;
	}

	@Deactivate
	public void deactivate(BundleContext bundleContext) {
		logService.log(LogService.LOG_WARNING, "deactivate " + getClass().getName());
		for (Entry<String, QueueComponent<T>> entry : existingServices.entrySet()) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					if (entry.getValue().stop()) {
						existingServices.remove(entry.getKey());
					}
				}
			});
		}
	}

	@Override
	public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				logService.log(LogService.LOG_INFO, "update configuration for " + getClass().getName());
				putDefaultQueueProperties(properties);
				createIfNotExistQueueComponent(pid, properties);
				Integer toUpdateQueueStatusCode = (Integer) properties.get(QueueConfigurationKeys.queueStatus.getKey());
				QueueStatus toUpdateQueueStatus = QueueStatus.fromStatusCode(toUpdateQueueStatusCode);
				QueueComponent<T> queueComponent = existingServices.get(pid);
				// TODO FH: NullPointer?
				queueComponent.changeStatusIfNeeded(toUpdateQueueStatus);
			}
		});
	}

	private void createIfNotExistQueueComponent(String pid, Dictionary<String, ?> properties) {
		QueueComponent<T> queueComponent;
		if (!existingServices.containsKey(pid)) {
			queueComponent = new QueueComponent<T>(this.getBundleContext(), properties);
			existingServices.put(pid, queueComponent);
		}
	}

	/*
	 * This method change not the configuration content. Delete the
	 * configuration entry manual.
	 *
	 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
	 */
	@Override
	public void deleted(String pid) {
		QueueComponent<T> queueComponent = existingServices.remove(pid);
		queueComponent.delete();
	}

	private void putDefaultQueueProperties(Dictionary<String, ?> properties) {

	}

	@Override
	public String getName() {
		return "QueueManager";
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
}
