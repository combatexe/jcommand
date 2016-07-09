package org.jcommand.queue.capability.service;

import java.util.Set;

import org.jcommand.provisioning.api.ProvisioningRepository;
import org.jcommand.provisioning.api.QueueProvisioningRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.serialization.JavaSerializer;

@Component(immediate=true, service={QueueProvisioningRepository.class, ProvisioningRepository.class}, enabled=true)
public class QueueCapabilityComponent implements QueueProvisioningRepository{

	Prevayler<CapabilityRoot> capabilityDB;
	
	@Activate
	public void activate(BundleContext bundleContext){
		PrevaylerFactory<CapabilityRoot> factory = new PrevaylerFactory<CapabilityRoot>();
		factory.configureSnapshotSerializer(new JavaSerializer(this.getClass().getClassLoader()));
		factory.configureJournalSerializer(new JavaSerializer(this.getClass().getClassLoader()));
		// TODO FH: add configuration system
		factory.configurePrevalenceDirectory("c:\\jcommand\\queue.persistence.provisioning");
		CapabilityRoot db = new CapabilityRoot();
		factory.configurePrevalentSystem(db);
		try {
			 capabilityDB = factory.create();
		} catch (Exception e) {
			// TODO FH: add logger
			e.printStackTrace();
		}
	}
	 
	@Deactivate
	public void deactivate(BundleContext bundleContext) throws Exception{
		capabilityDB.takeSnapshot();
	}
	
	@Override
	public void putQueueCapability(Long queueId, BundleCapability capability) {
		capabilityDB.execute(new SaveCapability(queueId, capability));
	} 
	
	// TODO FH: Exception concept? Wrap into RuntimeException?
	@Override
	public Long createNewQueueId() {
			return capabilityDB.execute(new CreateNewDBId());
	}

	@Override
	public Set<BundleCapability> getCapabilities(Long queueId) {
			return capabilityDB.execute(new QueryCapabilities(queueId));
	}
	
	@Override
	public Set<Long> getActiveIds(){
		return capabilityDB.execute(new QueryActiveQueueIds());
	}
}
