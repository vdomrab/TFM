package device.capacitivefuelsensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.FuelSensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	FuelSensorARC fuelSensorARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.fuelSensorARC = new FuelSensorARC(bundleContext, "CapacitiveFuelSensor");
		this.fuelSensorARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.fuelSensorARC.stop();
		this.fuelSensorARC = null;
		Activator.context = null;
	}

}
