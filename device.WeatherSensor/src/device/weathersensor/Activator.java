package device.weathersensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.WeatherSensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private WeatherSensorARC weatherSensorARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.weatherSensorARC = new WeatherSensorARC(bundleContext, "WeatherSensor");
		this.weatherSensorARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.weatherSensorARC.stop();
		this.weatherSensorARC = null;
		Activator.context = null;
	}

}
