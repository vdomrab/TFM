package device.egtsensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.EGTSensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	EGTSensorARC egtsensorARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		egtsensorARC = new EGTSensorARC(bundleContext, "EGTSensor");
		egtsensorARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		egtsensorARC.stop();
		egtsensorARC = null;
		Activator.context = null;
	}

}
