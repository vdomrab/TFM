package interaction.lowfuelwarning;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.interaction.ARC.LowFuelWarningARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private LowFuelWarningARC fuelWarningARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		fuelWarningARC = new LowFuelWarningARC(bundleContext, "LowFuelWarning");
		fuelWarningARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		fuelWarningARC.stop();
		fuelWarningARC = null;
		Activator.context = null;
		
	}

}
