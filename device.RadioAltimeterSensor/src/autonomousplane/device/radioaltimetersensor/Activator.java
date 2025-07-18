package autonomousplane.device.radioaltimetersensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.RadioAltimeterSensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private RadioAltimeterSensorARC radioAltimeterSensorARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;
		this.radioAltimeterSensorARC = new RadioAltimeterSensorARC(bundleContext, "RadioAltimeterSensor");
		this.radioAltimeterSensorARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.radioAltimeterSensorARC.stop();
		this.radioAltimeterSensorARC = null;
		Activator.context = null;
	}

}
