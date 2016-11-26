package org.jcommand.logging;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

@Component(immediate = true, enabled = true)
public class JCommandLoggerComponent implements LogListener {

	private ServiceReference<?> serviceReference;
	private Long bundleId;

	private String filter = System.getProperty("jcommand.log.bundle.filter");

	@Reference
	public void bindLogRederService(LogReaderService logReaderService) {
		logReaderService.addLogListener(this);
	}

	public void unbindLogRederService(LogReaderService logReaderService) {
		logReaderService.removeLogListener(this);
	}

	@Override
	public void logged(LogEntry logEntry) {
		Bundle bundle = logEntry.getBundle();
		if (filter != null && !bundle.getSymbolicName().matches(filter)) {
			return;
		}

		Long bundleCurrentId = logEntry.getBundle().getBundleId();
		if (bundleId != bundleCurrentId) {
			String bundleMsg = String.format("Bundle - %s:%s", logEntry.getBundle().getSymbolicName(),
					logEntry.getBundle().getVersion());
			System.out.println(bundleMsg);
			bundleId = bundleCurrentId;
		}

		ServiceReference<?> serviceReferenceCurrent = logEntry.getServiceReference();
		if (serviceReference != serviceReferenceCurrent) {
			if (serviceReferenceCurrent != null) {
				System.out.println(" Service - " + serviceReferenceCurrent.toString());
			}
			serviceReference = serviceReferenceCurrent;
		}

		String msg = String.format("  %s - %s -> %s", getLevel(logEntry.getLevel()), logEntry.getMessage(),
				logEntry.getTime());

		if (logEntry.getLevel() == LogService.LOG_ERROR) {
			System.err.println(msg);
			if (logEntry.getException() != null) {
				System.err.println(logEntry.getException());
			}
		} else {
			System.out.println(msg);
		}
	}

	private static String getLevel(int statusCode) {
		String status = "no define";
		switch (statusCode) {
		case LogService.LOG_ERROR:
			status = "ERROR";
			break;
		case LogService.LOG_DEBUG:
			status = "DEBUG";
			break;
		case LogService.LOG_WARNING:
			status = "WARNI";
			break;
		case LogService.LOG_INFO:
			status = "INFO ";
			break;
		case 0:
			status = "TEST ";
			break;
		}
		return status;
	}

}
