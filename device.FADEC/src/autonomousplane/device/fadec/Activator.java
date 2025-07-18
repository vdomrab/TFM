package autonomousplane.device.fadec;

import org.osgi.framework.BundleActivator;
import autonomousplane.infraestructure.devices.ARC.FADECARC;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private FADECARC fadecARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;
		this.fadecARC = new FADECARC(bundleContext, "FADEC");
		this.fadecARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.fadecARC.stop();
		this.fadecARC = null;
		Activator.context = null;
	}

}
