package org.jcommand.queue.manager;

import static org.jcommand.queue.manager.QueueStatus.DELETE;
import static org.jcommand.queue.manager.QueueStatus.EXIST;
import static org.jcommand.queue.manager.QueueStatus.MAXENTRIES;
import static org.jcommand.queue.manager.QueueStatus.RUNNING;
import static org.jcommand.queue.manager.QueueStatus.STOP;
import static org.jcommand.queue.manager.QueueStatus.STOPPING;
import static org.jcommand.queue.manager.QueueStatus.SUSPEND;
import static org.jcommand.queue.manager.QueueStatus.UNDEFINE;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

import org.jcommand.prevayler.classloader.PrevaylerBundleClassLoader;
import org.jcommand.queue.api.Queue;
import org.jcommand.queue.api.QueueObject;
import org.jcommand.queue.manager.capability.QueueCapability;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.serialization.JavaSerializer;

public class QueueComponent<T extends QueueObject> implements Queue<T>, BundleListener, Serializable {

	private static final long serialVersionUID = 1L;
	private final String pid;
	private final File queueLocation;
	private Integer maxQueueEntries;

	private int queueStatus = QueueStatus.STOP.statusCode;
	private transient final Dictionary<String, ?> properties;
	private transient Prevayler<PersistenceQueue<T>> persistenceQueue;
	private transient final BundleContext bundleContext;
	private transient ServiceRegistration<?> registerService;
	private transient PrevaylerBundleClassLoader bundleClassLoader;
	private QueueCapability queueCapability;

	public QueueComponent(BundleContext bundleContext, Dictionary<String, ?> properties) {
		this.bundleContext = bundleContext;
		this.properties = properties;
		pid = (String) properties.get(QueueConfigurationKeys.pid.getKey());
		String queueLocationString = (String) properties.get(QueueConfigurationKeys.queueLocation.getKey());
		queueLocation = new File(queueLocationString);
		maxQueueEntries = (Integer) properties.get(QueueConfigurationKeys.maxQueueEntries.getKey());
	}

	@Override
	public boolean offer(T toQueueObject) {
		if ((queueStatus & RUNNING.statusCode) == RUNNING.statusCode) {
			if (maxQueueEntries < persistenceQueue.prevalentSystem().queue.size()) {
				queueStatus |= MAXENTRIES.statusCode;
				suspend();
			}
			return persistenceQueue.execute(new OfferQueueObject<T>(toQueueObject));
		}
		return false;
	}

	@Override
	public List<T> poll(int poolSize) {
		int toCheckStatus = STOP.statusCode | STOPPING.statusCode;
		if ((queueStatus & toCheckStatus) == toCheckStatus) {
			return null;
		}
		if (maxQueueEntries >= persistenceQueue.prevalentSystem().queue.size()
				&& ((queueStatus & MAXENTRIES.statusCode) == MAXENTRIES.statusCode)) {
			queueStatus &= ~MAXENTRIES.statusCode;
			resume();
		}
		try {
			return persistenceQueue.execute(new PollQueueObject<T>(poolSize));
		} catch (Throwable throwable) {
			throw new RuntimeException("Can't pool queue object from queue", throwable);
		}
	}

	@Override
	public T poll() {
		List<T> pollResult = poll(1);
		return pollResult != null && !pollResult.isEmpty() ? pollResult.get(0) : null;
	}

	@Override
	public boolean commit(Long queueObjectId) {
		return commit(Arrays.asList(queueObjectId));
	}

	@Override
	public boolean commit(List<Long> queueObjectIds) {
		return persistenceQueue.execute(new CommitQueueObject<T>(queueObjectIds));
	}

	@Override
	public long getLoad() {
		return persistenceQueue.prevalentSystem().queue.size();
	}

	@Override
	public long getAvGCommandLiveQueue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long aggregateAvGExecuteTimeOfAllQueuedCommands() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getQueueStatus() {
		return QueueStatus.toString(queueStatus);
	}

	public synchronized boolean start() {

		if (!preconditionSatisfy()) {
			return false;
		}

		if (QueueStatus.hasStatus(queueStatus, STOP, EXIST)) {
			queueCapability = new QueueCapability(new File(queueLocation, "capability"));
			queueCapability.activate(bundleContext);
			bundleClassLoader = new PrevaylerBundleClassLoader(pid, queueCapability);
			bundleClassLoader.addBundle(bundleContext.getBundle());
			Bundle[] bundles = bundleContext.getBundles();
			for (Bundle bundle : bundles) {
				Dictionary<String, String> headers = bundle.getHeaders();
				if (null != headers.get("JCommand-Model")) {
					bundleClassLoader.addBundle(bundle);
				}
			}
			PrevaylerFactory<PersistenceQueue<T>> factory = new PrevaylerFactory<PersistenceQueue<T>>();
			JavaSerializer serializer = new JavaSerializer(bundleClassLoader);
			factory.configureJournalSerializer(serializer);
			factory.configureSnapshotSerializer(serializer);
			factory.configurePrevalenceDirectory(queueLocation.getAbsolutePath());
			factory.configurePrevalentSystem(new PersistenceQueue<T>());

			try {
				persistenceQueue = factory.create();
				queueStatus = QueueStatus.EXIST.statusCode;
				activateModelBundleListener();
				queueStatus |= QueueStatus.RUNNING.statusCode;
				registerService = bundleContext.registerService(Queue.class, QueueComponent.this, properties);
				return true;
			} catch (Exception e) {
				queueStatus |= QueueStatus.ERROR.statusCode;
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean preconditionSatisfy() {
		// TODO Auto-generated method stub
		return true;
	}

	public synchronized boolean stop() {

		queueStatus = QueueStatus.EXIST.statusCode;
		queueStatus |= QueueStatus.STOPPING.statusCode;
		registerService.unregister();
		registerService = null;
		try {
			persistenceQueue.takeSnapshot();
			persistenceQueue.close();
			persistenceQueue = null;
			deactivateModelBundleListener();
			queueStatus |= QueueStatus.STOP.statusCode;
		} catch (Exception e) {
			queueStatus |= QueueStatus.ERROR.statusCode;
			e.printStackTrace();
		}
		return (queueStatus & QueueStatus.ERROR.statusCode) != QueueStatus.ERROR.statusCode;

	}

	public synchronized boolean suspend() {
		queueStatus |= SUSPEND.statusCode;
		return true;
	}

	public synchronized boolean resume() {
		queueStatus &= ~SUSPEND.statusCode;
		return true;
	}

	/**
	 * No support yet
	 *
	 * @return always false
	 */
	public synchronized boolean delete() {
		// No support yet
		return false;
	}

	/**
	 * Support only RUNNING, STOP and SUSPEND
	 *
	 * @param toUpdateQueueStatus
	 */
	public void changeStatusIfNeeded(QueueStatus toUpdateQueueStatus) {
		// TODO FH: use threads to change status
		if (!assertValidStatus(toUpdateQueueStatus.statusCode)) {
			// TODO: logging inconsistent update status and do nothing
			return;
		}

		if (!assertValidStatus(queueStatus)) {
			// TODO: logging inconsistent status and do nothing
			return;
		}

		if (QueueStatus.hasStatus(toUpdateQueueStatus, RUNNING)) {
			if (QueueStatus.hasStatus(queueStatus, RUNNING)) {
				// Log is running statement;
			} else if (QueueStatus.hasStatus(queueStatus, EXIST, STOP, UNDEFINE)) {
				this.start();// start queue
			} else if (QueueStatus.hasStatus(queueStatus, SUSPEND)) {
				this.resume();// resume queue
			} else if (QueueStatus.hasStatus(queueStatus, DELETE)) {
				this.start();// restore if possible and start queue
			}
		} else if (QueueStatus.hasStatus(toUpdateQueueStatus, STOP)) {
			if (QueueStatus.hasStatus(queueStatus, RUNNING)) {
				this.stop();// stop queue; Reject new commands but
							// delivery all old commands
			} else if (QueueStatus.hasStatus(queueStatus, EXIST, STOP)) {
				// Log - queue is already stopped
			} else if (QueueStatus.hasStatus(queueStatus, SUSPEND)) {
				this.stop();// resume queue and move to stop status
			} else if (QueueStatus.hasStatus(queueStatus, DELETE)) {
				// log wrong request and do nothing
			} else if (QueueStatus.hasStatus(queueStatus, EXIST)) {
				// log wrong request and do nothing
			}
		} else if (QueueStatus.hasStatus(toUpdateQueueStatus, SUSPEND)) {
			if (QueueStatus.hasStatus(queueStatus, RUNNING)) {
				// suspend queue; Reject new commands and don't delivery all old
				// commands. No memory and persitence files clean. Service is
				// register but no Cpmmand put into queue.
				this.suspend();
			} else if (QueueStatus.hasStatus(queueStatus, EXIST, STOP)) {
				this.suspend();// Log - queue is already stopped
			} else if (QueueStatus.hasStatus(queueStatus, SUSPEND)) {
				// Log - queue is already suspend
			} else if (QueueStatus.hasStatus(queueStatus, DELETE)) {
				this.suspend(); // log wrong request and do nothing
			} else if (QueueStatus.hasStatus(queueStatus, EXIST)) {
				this.suspend();// log wrong request and do nothing
			}
		}
	}

	private boolean assertValidStatus(Integer updateQueueStatus) {
		// TODO Auto-generated method stub
		return true;
	}

	private void activateModelBundleListener() {
		bundleContext.addBundleListener(this);
		for (Bundle bundle : bundleContext.getBundles()) {
			Dictionary<String, String> headers = bundle.getHeaders();
			if (null != headers.get("JCommand-Model")) {
				bundleClassLoader.addBundle(bundle);
			}
		}
	}

	@Override
	public void bundleChanged(BundleEvent bundleEvent) {
		if (bundleEvent.getType() == BundleEvent.RESOLVED) {
			Bundle bundle = bundleEvent.getBundle();
			Dictionary<String, String> headers = bundle.getHeaders();
			if (null != headers.get("JCommand-Model")) {
				bundleClassLoader.addBundle(bundle);
				if (bundleClassLoader.areCapabilitiesSatisfy()) {
					resume();
				}
			}
		} else if (bundleEvent.getType() == BundleEvent.UNRESOLVED) {
			Bundle bundle = bundleEvent.getBundle();
			Dictionary<String, String> headers = bundle.getHeaders();
			if (null != headers.get("JCommand-Model")) {
				bundleClassLoader.removeBundle(bundle);
				if (!bundleClassLoader.areCapabilitiesSatisfy()) {
					suspend();
				}
			}
		}
	}

	private void deactivateModelBundleListener() {
		bundleContext.removeBundleListener(this);
		queueCapability.deactivate(bundleContext);
		bundleClassLoader = null;
	}

}
