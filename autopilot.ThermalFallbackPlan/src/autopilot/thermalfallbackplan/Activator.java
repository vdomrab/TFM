package autopilot.thermalfallbackplan;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private ThermalFallbackPlanARC thermalFallbackPlanARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		thermalFallbackPlanARC = new ThermalFallbackPlanARC(bundleContext, "ThermalFallbackPlan");
		thermalFallbackPlanARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		thermalFallbackPlanARC.stop();
		thermalFallbackPlanARC = null;
		Activator.context = null;
	}

}
