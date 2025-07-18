package autonomousplane.l2_partialautomation;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	L2_PartialAutomationARC partialAutomationARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.partialAutomationARC = new L2_PartialAutomationARC(bundleContext, "L2_PartialAutomation");
		this.partialAutomationARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.partialAutomationARC.stop();
		this.partialAutomationARC = null;
		Activator.context = null;
	}

}
