package device.gnss;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.GNSSARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private GNSSARC gnssARS = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.gnssARS = new GNSSARC(bundleContext, "GNSS");
		this.gnssARS.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.gnssARS.stop();
		this.gnssARS = null;
		Activator.context = null;

	}

}
