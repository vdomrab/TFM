package autonomousplane.device.altitudesensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

 import autonomousplane.infraestructure.devices.ARC.AltitudeSensorARC;

public class Activator implements BundleActivator {


	private static BundleContext context;
	private AltitudeSensorARC serviceARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;
		this.serviceARC = new AltitudeSensorARC(bundleContext, "AltitudeSensor");
		this.serviceARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.serviceARC.stop();
		this.serviceARC = null;
		Activator.context = null;
	}


}
