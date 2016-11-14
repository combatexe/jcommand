package org.jcommand.queue.manager.capability;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.wiring.BundleCapability;

class CapabilityRoot implements Serializable {

	private static final long serialVersionUID = 1L;

	Set<BundleCapability> repository = new HashSet<BundleCapability>(1000);
}