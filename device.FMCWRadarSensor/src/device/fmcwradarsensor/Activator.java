package device.fmcwradarsensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.ProximitySensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private ProximitySensorARC proximitySensor = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		proximitySensor = new ProximitySensorARC(bundleContext, "FMCWRadarSensor");
		proximitySensor.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		proximitySensor.stop();
		proximitySensor = null;
		Activator.context = null;
	}

}
