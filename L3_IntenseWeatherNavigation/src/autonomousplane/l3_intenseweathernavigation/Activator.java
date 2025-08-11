package autonomousplane.l3_intenseweathernavigation;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	L3_IntenseWeatherNavigationARC intenseWeatherNavigationARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.intenseWeatherNavigationARC = new L3_IntenseWeatherNavigationARC(bundleContext, "L3_IntenseWeatherNavigation");
		this.intenseWeatherNavigationARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.intenseWeatherNavigationARC.stop();
		this.intenseWeatherNavigationARC = null;
		Activator.context = null;
	}

}
