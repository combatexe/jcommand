package org.jcommand.queue.manager.capability;

import java.util.Date;
import java.util.Set;

import org.osgi.framework.wiring.BundleCapability;
import org.prevayler.SureTransactionWithQuery;

public class QueryCapabilities implements SureTransactionWithQuery<CapabilityRoot, Set<BundleCapability>> {

	private static final long serialVersionUID = 8938700820189379650L;

	public QueryCapabilities() {
	}

	@Override
	public Set<BundleCapability> executeAndQuery(CapabilityRoot root, Date arg1) {
		return root.repository;
	}
}
