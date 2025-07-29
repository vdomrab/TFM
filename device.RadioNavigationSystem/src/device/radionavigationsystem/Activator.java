package device.radionavigationsystem;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.NavigationSystemARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private NavigationSystemARC navigationSystemARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		navigationSystemARC = new NavigationSystemARC(bundleContext, "RadioNavigationSystem");
		navigationSystemARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		navigationSystemARC.stop();
		navigationSystemARC = null;
		Activator.context = null;
	}

}
