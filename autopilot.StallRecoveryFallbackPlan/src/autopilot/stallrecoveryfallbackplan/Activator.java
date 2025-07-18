package autopilot.stallrecoveryfallbackplan;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	protected StallRecoveryFallbackPlanARC stallRecoveryFallbackPlanARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.stallRecoveryFallbackPlanARC = new StallRecoveryFallbackPlanARC(bundleContext, "StallRecoveryFallbackPlan");
		this.stallRecoveryFallbackPlanARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.stallRecoveryFallbackPlanARC.stop();
		this.stallRecoveryFallbackPlanARC = null;
		Activator.context = null;
	}

}
