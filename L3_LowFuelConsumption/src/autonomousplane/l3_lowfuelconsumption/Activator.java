package autonomousplane.l3_lowfuelconsumption;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	L3_LowFuelConsumptionARC lowFuelConsumptionARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.lowFuelConsumptionARC = new L3_LowFuelConsumptionARC(bundleContext, "LowFuelConsumption");
		this.lowFuelConsumptionARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.lowFuelConsumptionARC.stop();
		this.lowFuelConsumptionARC = null;
		Activator.context = null;
	}

}
