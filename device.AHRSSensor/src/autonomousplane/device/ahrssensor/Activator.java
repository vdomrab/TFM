package autonomousplane.device.ahrssensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.AHRSSensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private  AHRSSensorARC ahrsSensorARS = null;
	static BundleContext getContext() {
		return context;
	
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.ahrsSensorARS = new AHRSSensorARC(bundleContext, "AHRSSensor");
		this.ahrsSensorARS.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.ahrsSensorARS.stop();
		this.ahrsSensorARS = null;
		Activator.context = null;
	}

}
