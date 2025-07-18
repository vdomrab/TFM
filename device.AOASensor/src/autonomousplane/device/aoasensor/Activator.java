package autonomousplane.device.aoasensor;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.AOASensorARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private AOASensorARC aoaArc = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;
		this.aoaArc = new AOASensorARC(bundleContext, "AOASensor");
		this.aoaArc.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.aoaArc.stop();
		this.aoaArc = null;
		Activator.context = null;
	}

}
