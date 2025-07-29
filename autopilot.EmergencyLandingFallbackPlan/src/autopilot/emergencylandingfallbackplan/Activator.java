package autopilot.emergencylandingfallbackplan;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private EmergencyLandingFallbackPlanARC emergencyLandingFallbackPlanARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		emergencyLandingFallbackPlanARC = new EmergencyLandingFallbackPlanARC(bundleContext, "EmergencyLandingFallbackPlan");
		emergencyLandingFallbackPlanARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		emergencyLandingFallbackPlanARC.stop();
		emergencyLandingFallbackPlanARC = null;
		Activator.context = null;
	}

}
