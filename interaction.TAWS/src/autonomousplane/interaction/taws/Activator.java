package autonomousplane.interaction.taws;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.interaction.ARC.TAWSARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private TAWSARC serviceARC = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;
		this.serviceARC = new TAWSARC(bundleContext, "TAWS");
		this.serviceARC.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.serviceARC.stop();
		this.serviceARC = null;
		Activator.context = null;
	}


}
