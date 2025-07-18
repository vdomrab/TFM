package autonomousplane.device.controlsurfaces;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.ARC.ControlSurfacesARC;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private ControlSurfacesARC controlSurfacesARS = null;
	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		this.controlSurfacesARS = new ControlSurfacesARC(bundleContext, "ControlSurfaces");
		this.controlSurfacesARS.start();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		this.controlSurfacesARS.stop();
		this.controlSurfacesARS = null;
		Activator.context = null;
	}

}
