package autonomousplane.interaction.stallwarning;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.interaction.ARC.StallWarningARC;



public class Activator implements BundleActivator {

	private static BundleContext context;
	protected StallWarningARC serviceARC = null;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;
		this.serviceARC = new StallWarningARC(bundleContext, "StallWarning");
		this.serviceARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.serviceARC.stop();
		this.serviceARC = null;
		Activator.context = null;
	}
}


