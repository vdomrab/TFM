package device.etl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.ETLARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	ETLARC etlARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		etlARC = new ETLARC(bundleContext, "ETL");
		etlARC.deploy();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		etlARC.undeploy();
		etlARC = null;
		Activator.context = null;
	}

}
