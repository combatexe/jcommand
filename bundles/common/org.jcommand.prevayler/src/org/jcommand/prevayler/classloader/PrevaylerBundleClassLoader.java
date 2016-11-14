package org.jcommand.prevayler.classloader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jcommand.provisioning.api.ProvisioningRepository;
import org.osgi.framework.Bundle;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;

public class PrevaylerBundleClassLoader extends ClassLoader {

	private Set<Bundle> bundles = new HashSet<Bundle>();

	private final ProvisioningRepository provisioningRepository;

	// TODO FH: search the next version with the new version db service
	public PrevaylerBundleClassLoader(String queueId, ProvisioningRepository queueProvisioningRepository) {
		this.provisioningRepository = queueProvisioningRepository;
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
		if (provisioningRepository == null) {
			return;
		}
		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		List<BundleCapability> capabilities = wiring.getCapabilities(PackageNamespace.PACKAGE_NAMESPACE);
		for (BundleCapability capability : capabilities) {
			if (currentPackage.getName().equals(capability.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE))) {
				provisioningRepository.addCapability(capability);
				// No break because multi entries with same package name but
				// different version
			}
		}
	}

	public boolean areCapabilitiesSatisfy() {
		boolean exist = true;
		for (BundleCapability queueCapability : provisioningRepository.getCapabilities()) {
			exist = false;
			for (Bundle bundle : bundles) {
				BundleWiring wiring = bundle.adapt(BundleWiring.class);
				List<BundleCapability> capabilities = wiring.getCapabilities(PackageNamespace.PACKAGE_NAMESPACE);
				if (queueCapability.equals(capabilities)) {
					exist = true;
				}
			}
			if (!exist) {
				return false;
			}
		}
		return true;
	}
}
