package device.svldsystem;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.LandingSystemARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private LandingSystemARC landingSystemARC = null;
	
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		landingSystemARC = new LandingSystemARC(bundleContext, "SVLDSystem");
		landingSystemARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		landingSystemARC.stop();
		landingSystemARC = null;
		Activator.context = null;
		
	}

}
