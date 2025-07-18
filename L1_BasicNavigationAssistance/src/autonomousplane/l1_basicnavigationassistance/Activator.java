package autonomousplane.l1_basicnavigationassistance;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	L1_BasicNavigationAssistanceARC basicNavigationAssistanceARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.basicNavigationAssistanceARC = new L1_BasicNavigationAssistanceARC(bundleContext, "L1_BasicNavigationAssistance");
		this.basicNavigationAssistanceARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.basicNavigationAssistanceARC.stop();
		this.basicNavigationAssistanceARC = null;
		Activator.context = null;
	}

}
