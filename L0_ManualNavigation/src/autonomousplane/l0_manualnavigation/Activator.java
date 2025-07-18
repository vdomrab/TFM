package autonomousplane.l0_manualnavigation;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	protected L0_ManualNavigationARC navigationFunctionARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.navigationFunctionARC = new L0_ManualNavigationARC(bundleContext, "L0_ManualNavigation");
		this.navigationFunctionARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.navigationFunctionARC.stop();
		this.navigationFunctionARC = null;
		Activator.context = null;
	}

}
