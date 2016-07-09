package org.jcommand.provisioning.api;

import java.util.Set;

import org.osgi.framework.wiring.BundleCapability;

public interface ProvisioningRepository {

	Set<BundleCapability> getCapabilities(Long capabilityId);

	Set<Long> getActiveIds();
	
}
