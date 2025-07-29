package interaction.frozenwarning;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.interaction.ARC.FrozenWarningARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private FrozenWarningARC frozenWarningARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		frozenWarningARC = new FrozenWarningARC(bundleContext, "FrozenWarning");
		frozenWarningARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		frozenWarningARC.stop();
		frozenWarningARC = null;
		Activator.context = null;
		
	}

}
