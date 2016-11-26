package org.jcommand.integration.test.queue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.jcommand.cm.ConfigurationService;
import org.jcommand.queue.api.Queue;
import org.jcommand.queue.api.QueueObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class QueueTest {

	private static Queue<QueueObject> queueService;
	private static ConfigurationService configurationService;

	public static final int WAIT_TIME = 60;
	@ClassRule
	public static Timeout globalTimeout = Timeout.seconds(WAIT_TIME);
	private static LogService logService;

	@BeforeClass
	public static void findServices() {
		// findLogService();
		// findConfigService();
		findQueueService();
	}

	private static void findLogService() {
		logService = findService(LogService.class);
		assertThat("Log service not exist", configurationService, notNullValue());
	}

	private static void findConfigService() {
		configurationService = findService(ConfigurationService.class);
		assertThat("Configuration service not exist", configurationService, notNullValue());
	}

	@SuppressWarnings("unchecked")
	public static void findQueueService() {
		queueService = findService(Queue.class);
		assertThat("Queue service not exist", queueService, notNullValue());
	}

	public static <T> T findService(Class<T> clazz) {
		BundleContext bundleContext = FrameworkUtil.getBundle(QueueTest.class).getBundleContext();
		ServiceTracker<T, Object> serviceTracker = new ServiceTracker<>(bundleContext, clazz, null);
		serviceTracker.open();
		int waitTime = WAIT_TIME;
		while (serviceTracker.isEmpty()) {
			try {
				System.out.println("wait for Configuration service:" + waitTime--);
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return (T) serviceTracker.getService();
	}

	@Test
	public void smokeTest() {
		QueueTestObject testQueueObject = new QueueTestObject();
		queueService.offer(testQueueObject);
		List<QueueObject> result = queueService.poll(10);
		assertThat("poll result is", result, notNullValue());
		assertThat("poll result is", result, hasSize(1));
		assertThat("poll result is", result.get(0), notNullValue());
		// assertThat("poll result is", result.get(0),
		// equalTo(testQueueObject));
	}
}
