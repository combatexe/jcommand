package org.jcommand.queue.modelextender;

import java.util.Dictionary;

import org.jcommand.prevayler.classloader.PrevaylerBundleClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(enabled = true, name = "org.jcommand.queue.extender", immediate = true)
public class ModelSerializeExtender {

	private ModelExtenderBundleListener bundleListener;

	@Activate
	public void activate(BundleContext bundleContext) {

		// TODO FH: remove dbVersion with dynamic max version - association dbVersion with PrevaylerBundleClassLoader
		final PrevaylerBundleClassLoader bundleClassLoader = new PrevaylerBundleClassLoader(1);
		bundleListener = new ModelExtenderBundleListener(bundleClassLoader);

		bundleContext.addBundleListener(bundleListener);

		for (Bundle bundle : bundleContext.getBundles()) {
			Dictionary<String, String> headers = bundle.getHeaders();
			if (null != headers.get("JCommand-Model")) {
				bundleClassLoader.addBundle(bundle);
			}
		}
	}

	@Deactivate
	public void deactivate(BundleContext bundleContext) {
		bundleContext.removeBundleListener(bundleListener);
	}

	private final class ModelExtenderBundleListener implements BundleListener {
		private final PrevaylerBundleClassLoader bundleClassLoader;

		private ModelExtenderBundleListener(PrevaylerBundleClassLoader bundleClassLoader) {
			this.bundleClassLoader = bundleClassLoader;
		}

		@Override
		public void bundleChanged(BundleEvent bundleEvent) {
			if (bundleEvent.getType() == BundleEvent.RESOLVED) {
				Bundle bundle = bundleEvent.getBundle();
				Dictionary<String, String> headers = bundle.getHeaders();
				if (null != headers.get("JCommand-Model")) {
					bundleClassLoader.addBundle(bundle);
				}
			} else if (bundleEvent.getType() == BundleEvent.UNRESOLVED) {
				Bundle bundle = bundleEvent.getBundle();
				Dictionary<String, String> headers = bundle.getHeaders();
				if (null != headers.get("JCommand-Model")) {
					// TODO FH: check remove possible?
					bundleClassLoader.removeBundle(bundle);
				}
			}
		}
	}
}
