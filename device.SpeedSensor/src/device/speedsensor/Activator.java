package device.speedsensor;

import autonomousplane.infraestructure.devices.SpeedSensor;
import autonomousplane.infraestructure.devices.ARC.SpeedSensorARC;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private SpeedSensorARC speedSensorARS = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.speedSensorARS = new SpeedSensorARC(bundleContext, "SpeedSensor");
		this.speedSensorARS.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.speedSensorARS.stop();
		this.speedSensorARS = null;
		Activator.context = null;
	}

}
