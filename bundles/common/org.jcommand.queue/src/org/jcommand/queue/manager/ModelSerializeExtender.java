package org.jcommand.queue.manager;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

import org.jcommand.prevayler.classloader.PrevaylerBundleClassLoader;
import org.jcommand.provisioning.api.QueueProvisioningRepository;
import org.jcommand.queue.manager.api.QueueManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.serialization.JavaSerializer;

@Component(enabled = true, immediate = true)
public class ModelSerializeExtender implements QueueManager {

	private ModelExtenderBundleListener bundleListener;
	private QueueProvisioningRepository queueProvisioningRepository;

	private List<PrevaylerBundleClassLoader> bundleClassLoaders = new ArrayList<PrevaylerBundleClassLoader>();
	private List<Prevayler<CommandQueue>> commandQueues = new ArrayList<Prevayler<CommandQueue>>();

	@Activate
	public void activate(BundleContext bundleContext) {

		Set<Long> activeQueueIds = queueProvisioningRepository.getActiveIds();

		for (Long queueId : activeQueueIds) {
			createQueue(queueId);
		}

		// TODO FH: remove dbVersion with dynamic max version - association
		// dbVersion with PrevaylerBundleClassLoader
		bundleListener = new ModelExtenderBundleListener(bundleClassLoaders);

		bundleContext.addBundleListener(bundleListener);

		for (Bundle bundle : bundleContext.getBundles()) {
			Dictionary<String, String> headers = bundle.getHeaders();
			if (null != headers.get("JCommand-Model")) {
				for (PrevaylerBundleClassLoader bundleClassLoader : bundleClassLoaders) {
					bundleClassLoader.addBundle(bundle);
				}
			}
		}
	}

	private void createQueue(Long queueId) {
		final PrevaylerBundleClassLoader bundleClassLoader = new PrevaylerBundleClassLoader(queueId,
				queueProvisioningRepository);
		bundleClassLoaders.add(bundleClassLoader);
		
		PrevaylerFactory<CommandQueue> factory = new PrevaylerFactory<CommandQueue>();
		JavaSerializer serializer = new JavaSerializer(bundleClassLoader);
		factory.configureJournalSerializer(serializer);
		factory.configureSnapshotSerializer(serializer);
		factory.configurePrevalenceDirectory("c:\\jcommand\\" + queueId);
		
		try {
			Prevayler<CommandQueue> commandQueue = factory.create();
			commandQueues.add(commandQueue);
		} catch (Exception e) {
			// TODO FH: Logger and exception handling?
			e.printStackTrace();
		}

	}

	@Deactivate
	public void deactivate(BundleContext bundleContext) {
		bundleContext.removeBundleListener(bundleListener);
	}

	private final class ModelExtenderBundleListener implements BundleListener {
		private final List<PrevaylerBundleClassLoader> bundleClassLoaders;

		private ModelExtenderBundleListener(List<PrevaylerBundleClassLoader> bundleClassLoaders) {
			this.bundleClassLoaders = bundleClassLoaders;
		}

		@Override
		public void bundleChanged(BundleEvent bundleEvent) {
			if (bundleEvent.getType() == BundleEvent.RESOLVED) {
				Bundle bundle = bundleEvent.getBundle();
				Dictionary<String, String> headers = bundle.getHeaders();
				if (null != headers.get("JCommand-Model")) {
					for (PrevaylerBundleClassLoader bundleClassLoader : bundleClassLoaders) {
						bundleClassLoader.addBundle(bundle);
					}
				}
			} else if (bundleEvent.getType() == BundleEvent.UNRESOLVED) {
				Bundle bundle = bundleEvent.getBundle();
				Dictionary<String, String> headers = bundle.getHeaders();
				if (null != headers.get("JCommand-Model")) {
					// TODO FH: check remove possible?
					for (PrevaylerBundleClassLoader bundleClassLoader : bundleClassLoaders) {
						bundleClassLoader.removeBundle(bundle);
					}
				}
			}
		}
	}

	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	public void bindQueueProvisioningRepository(QueueProvisioningRepository queueProvisioningRepository) {
		this.queueProvisioningRepository = queueProvisioningRepository;
	}

	public void unbindQueueProvisioningRepository(QueueProvisioningRepository queueProvisioningRepository) {
		this.queueProvisioningRepository = null;
	}

	@Override
	public void createQueue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeQueue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void suspendQueue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resumQueue() {
		// TODO Auto-generated method stub
		
	}

}
