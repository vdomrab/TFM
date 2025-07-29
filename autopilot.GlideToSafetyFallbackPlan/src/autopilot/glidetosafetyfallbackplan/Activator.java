package autopilot.glidetosafetyfallbackplan;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private GlideToSafetyFallbackPlanARC glideToSafetyFallbackPlanARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		glideToSafetyFallbackPlanARC = new GlideToSafetyFallbackPlanARC(bundleContext, "GlideToSafetyFallbackPlan");
		glideToSafetyFallbackPlanARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		glideToSafetyFallbackPlanARC.stop();
		glideToSafetyFallbackPlanARC = null;
		Activator.context = null;
	}

}
