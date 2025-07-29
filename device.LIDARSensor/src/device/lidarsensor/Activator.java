package device.lidarsensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.ProximitySensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private ProximitySensorARC lidarSensorARC = null;
	
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.lidarSensorARC = new ProximitySensorARC(bundleContext, "LIDARSensor");
		this.lidarSensorARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.lidarSensorARC.stop();
		this.lidarSensorARC = null;
		Activator.context = null;
	}

}
