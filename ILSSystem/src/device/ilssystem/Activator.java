package device.ilssystem;

import autonomousplane.infraestructure.devices.ARC.LandingSystemARC;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private LandingSystemARC ilsSystemARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.ilsSystemARC = new LandingSystemARC(bundleContext, "ILSSystem");
		this.ilsSystemARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.ilsSystemARC.stop();
		this.ilsSystemARC = null;
		Activator.context = null;
	}

}
