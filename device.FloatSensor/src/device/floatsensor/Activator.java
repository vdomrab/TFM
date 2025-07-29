package device.floatsensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.FuelSensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private FuelSensorARC fuelSensor = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		fuelSensor = new FuelSensorARC(bundleContext, "FloatSensor");
		fuelSensor.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		fuelSensor.stop();
		fuelSensor = null;
		Activator.context = null;
	}

}
