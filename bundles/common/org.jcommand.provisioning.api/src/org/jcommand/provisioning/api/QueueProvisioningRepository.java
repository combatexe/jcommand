package org.jcommand.provisioning.api;

import org.osgi.framework.wiring.BundleCapability;

public interface QueueProvisioningRepository extends ProvisioningRepository{

	void putQueueCapability(Long queueId, BundleCapability capability);

	Long createNewQueueId() throws Exception;

}
