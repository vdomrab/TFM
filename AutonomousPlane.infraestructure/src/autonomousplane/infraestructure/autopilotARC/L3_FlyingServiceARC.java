package autonomousplane.infraestructure.autopilotARC;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL3_FlyingService;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;

public class L3_FlyingServiceARC extends L2_FlyingServiceARC {
	public static final String REQUIRED_FUELSENSOR = "required_fuelsensor";
	public static final String REQUIRED_EGTSENSOR = "required_egtsensor";
	public static final String REQUIRED_PROXIMITYSENSOR = "required_proximitysensor";
	public static final String REQUIRED_LANDINGSYSTEM = "required_landingsystem";
	
	public L3_FlyingServiceARC(BundleContext context, String id) {
		super(context, id);
	}
	
	public IL3_FlyingService getTheL3FlyingService() {
		return (IL3_FlyingService) this.getTheFlyingService();
	}
	@Override
	public IAdaptiveReadyComponent  bindService(String req, Object value) {
		
		if (req.equals(REQUIRED_FUELSENSOR)) {
			this.getTheL3FlyingService().setFuelSensor((autonomousplane.devices.interfaces.IFuelSensor) value);
		} else if (req.equals(REQUIRED_EGTSENSOR)) {
			this.getTheL3FlyingService().setEGTSensor((autonomousplane.devices.interfaces.IEGTSensor) value);
		} else if (req.equals(REQUIRED_PROXIMITYSENSOR)) {
			this.getTheL3FlyingService().setProximitySensor((autonomousplane.devices.interfaces.IProximitySensor) value);
		} else if (req.equals(REQUIRED_LANDINGSYSTEM)) {
			this.getTheL3FlyingService().setLandingSystem((autonomousplane.devices.interfaces.ILandingSystem) value);
		
		} else {
			return super.bindService(req, value);
		}
		return this;
	}
	
	public IAdaptiveReadyComponent unbindService(String req, Object value) {
		if (req.equals(REQUIRED_FUELSENSOR)) {
			this.getTheL3FlyingService().setFuelSensor(null);
		} else if (req.equals(REQUIRED_EGTSENSOR)) {
			this.getTheL3FlyingService().setEGTSensor(null);
		} else if (req.equals(REQUIRED_PROXIMITYSENSOR)) {
			this.getTheL3FlyingService().setProximitySensor(null);
		} else if (req.equals(REQUIRED_LANDINGSYSTEM)) {
			this.getTheL3FlyingService().setLandingSystem(null);
		} else {
			return super.unbindService(req, value);
		}
		return this;
	}
	
	
	
}
