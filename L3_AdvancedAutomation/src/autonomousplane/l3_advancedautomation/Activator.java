package autonomousplane.l3_advancedautomation;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	L3_AdvancedAutomationARC advancedAutomationARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.advancedAutomationARC = new L3_AdvancedAutomationARC(bundleContext, "L3_AdvancedAutomation");
		this.advancedAutomationARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.advancedAutomationARC.stop();
		this.advancedAutomationARC = null;
		Activator.context = null;
	}

}
