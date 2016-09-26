package org.jcommand.prevayler.classloader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jcommand.provisioning.api.QueueProvisioningRepository;
import org.osgi.framework.Bundle;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;

public class PrevaylerBundleClassLoader extends ClassLoader {

	private Set<Bundle> bundles = new HashSet<Bundle>();
	private Long queueId;

	private QueueProvisioningRepository queueProvisioningRepository;

	// TODO FH: search the next version with the new version db service
	public PrevaylerBundleClassLoader(Long queueId, QueueProvisioningRepository queueProvisioningRepository) {
		this.queueId = queueId;
		this.queueProvisioningRepository = queueProvisioningRepository;
	}

	public void addBundle(Bundle bundle) {
		bundles.add(bundle);
	}

	public void removeBundle(Bundle bundle) {
		bundles.remove(bundle);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		for (Bundle bundle : bundles) {
			try {
				Class<?> clazz = bundle.loadClass(name);
				if (clazz != null) {
					trackCapability(bundle, clazz.getPackage());
					return clazz;
				}
			} catch (ClassNotFoundException cnfe) {
				// check next classloader
			}
		}
		throw new ClassNotFoundException("Prevayler ClassLoader can't find class:" + name);
	}

	private void trackCapability(Bundle bundle, Package currentPackage) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);

		List<BundleCapability> capabilities = wiring.getCapabilities(PackageNamespace.PACKAGE_NAMESPACE);
		for (BundleCapability capability : capabilities) {
			if (currentPackage.getName().equals(capability.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE))) {
				queueProvisioningRepository.putQueueCapability(queueId, capability);
				// No break because multi entries with same package name but different version
			}
		}
	}

}
