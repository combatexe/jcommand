package org.jcommand.provisioning.api;

import java.util.Set;

import org.osgi.framework.wiring.BundleCapability;

public interface ProvisioningRepository {

	Set<BundleCapability> getCapabilities();

	void addCapability(BundleCapability capability);

}
