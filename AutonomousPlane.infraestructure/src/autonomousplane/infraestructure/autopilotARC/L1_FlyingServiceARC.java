package autonomousplane.infraestructure.autopilotARC;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL1_FlyingService;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;

public class L1_FlyingServiceARC extends L0_FlyingServiceARC {

	public static String REQUIRED_AHRSSENSOR = "required_AHRSSensor";
	public static String REQUIRED_CONTROLSURFACES = "required_ControlSurfaces";

	public L1_FlyingServiceARC(BundleContext context, String bundleId) {
		super(context, bundleId);
	}

	protected IL1_FlyingService getTheL1FlyingService() {
		return (IL1_FlyingService) this.getTheFlyingService();
	}

	@Override
	public IAdaptiveReadyComponent bindService(String req, Object value) {
		if (req.equals(REQUIRED_AHRSSENSOR))
			this.getTheL1FlyingService().setAHRSSensor((IAttitudeSensor) value);
		else if (req.equals(REQUIRED_CONTROLSURFACES))
			this.getTheL1FlyingService().setControlSurfaces((IControlSurfaces) value);
		return super.bindService(req, value);
	}

	@Override
	public IAdaptiveReadyComponent unbindService(String req, Object value) {
		if (req.equals(REQUIRED_AHRSSENSOR))
			this.getTheL1FlyingService().setAHRSSensor(null);
		else if (req.equals(REQUIRED_CONTROLSURFACES))
			this.getTheL1FlyingService().setControlSurfaces(null);
		return super.unbindService(req, value);
	}

}
