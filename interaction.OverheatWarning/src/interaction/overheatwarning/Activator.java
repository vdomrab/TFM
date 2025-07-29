package interaction.overheatwarning;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.interaction.ARC.OverheatWarningARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private OverheatWarningARC overheatWarningARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		overheatWarningARC = new OverheatWarningARC(bundleContext, "OverheatWarning");
		overheatWarningARC.start();
		
	}

	public void stop(BundleContext bundleContext) throws Exception {
		overheatWarningARC.stop();
		overheatWarningARC = null;
		Activator.context = null;
		
	}

}
