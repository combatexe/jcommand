package org.jcommand.queue.manager.capability;

import java.io.File;
import java.util.Set;

import org.jcommand.provisioning.api.ProvisioningRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleCapability;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.serialization.JavaSerializer;

public class QueueCapability implements ProvisioningRepository {

	private Prevayler<CapabilityRoot> capabilityDB;
	private File storagePath;

	public QueueCapability(File storagePath) {
		this.storagePath = storagePath;
	}

	public boolean activate(BundleContext bundleContext) {
		PrevaylerFactory<CapabilityRoot> factory = new PrevaylerFactory<CapabilityRoot>();
		factory.configureSnapshotSerializer(new JavaSerializer(this.getClass().getClassLoader()));
		factory.configureJournalSerializer(new JavaSerializer(this.getClass().getClassLoader()));
		// TODO FH: add configuration system
		factory.configurePrevalenceDirectory(storagePath.getAbsolutePath());
		CapabilityRoot db = new CapabilityRoot();
		factory.configurePrevalentSystem(db);
		try {
			capabilityDB = factory.create();
			return true;
		} catch (Exception e) {
			// TODO FH: add logger and RuntimeException
			e.printStackTrace();
			return false;
		}
	}

	public boolean deactivate(BundleContext bundleContext) {
		try {
			capabilityDB.takeSnapshot();
			capabilityDB.close();
			capabilityDB = null;
			return true;
		} catch (Exception e) {
			// TODO FH: log error
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Set<BundleCapability> getCapabilities() {
		return capabilityDB.execute(new QueryCapabilities());
	}

	@Override
	public void addCapability(BundleCapability capability) {
		capabilityDB.execute(new SaveCapability(capability));
	}
}
