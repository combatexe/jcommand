package org.jcommand.integration.test.queue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.List;

import org.jcommand.queue.api.Queue;
import org.jcommand.queue.api.QueueObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

public class QueueTest {

	private static Queue<QueueObject> queueService;

	@ClassRule
	public static Timeout globalTimeout = Timeout.seconds(60);

	@BeforeClass()
	public static void findQueueService() {
		BundleContext bundleContext = FrameworkUtil.getBundle(QueueTest.class).getBundleContext();
		printBundleStatus(bundleContext);
		ServiceTracker<Queue, Object> serviceTracker = new ServiceTracker<>(bundleContext, Queue.class, null);
		serviceTracker.open();
		int waitCount = 100;
		while (serviceTracker.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		queueService = (Queue<QueueObject>) serviceTracker.getService();
		assertThat("Queue service not exist", queueService, notNullValue());
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

	private static void printBundleStatus(BundleContext bundleContext) {
		Bundle[] bundles = bundleContext.getBundles();
		for (Bundle bundle : bundles) {
			System.out.println(bundle.getSymbolicName());
			System.out.println(Arrays.toString(bundle.getRegisteredServices()));
			System.out.println(getStatus(bundle.getState()));
			System.out.println("---");
		}
	}

	private static String getStatus(int statusCode) {
		String status = "no define";
		switch (statusCode) {
		case Bundle.ACTIVE:
			status = "ACTIVE";
			break;
		case Bundle.INSTALLED:
			status = "INSTALLED";
			break;
		case Bundle.RESOLVED:
			status = "RESOLVED";
			break;
		case Bundle.STARTING:
			status = "STARTING";
			break;
		case Bundle.STOPPING:
			status = "STOPPING";
			break;
		case Bundle.UNINSTALLED:
			status = "UNINSTALLED";
			break;
		}
		return status;
	}
}
