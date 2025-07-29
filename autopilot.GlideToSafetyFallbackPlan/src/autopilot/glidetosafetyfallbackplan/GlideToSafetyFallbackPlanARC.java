package autopilot.glidetosafetyfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IGlideToSafetyFallbackPlan;
import autonomousplane.autopilot.interfaces.IThermalFallbackPlan;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IETL;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.infraestructure.autopilotARC.FallbackPlanARC;
import autonomousplane.infraestructure.devices.RadioAltimeterSensor;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class GlideToSafetyFallbackPlanARC extends FallbackPlanARC{
	public static String REQUIRED_RADIOALTIMETER = "required_radioaltimeter";
	public static String REQUIRED_CONTROLSURFACES = "required_controlsurfaces";
	public static String REQUIRED_ATTITUDESENSOR = "required_attitudesensor";
	public static String REQUIRED_ALTITUDESENSOR = "required_altitudesensor";
	public static String REQUIRED_ETL = "required_etl";
	public static String REQUIRED_SPEEDSENSOR = "required_speedsensor";
	public static String REQUIRED_PROXIMITYSENSOR = "required_proximitysensor";
	
	public GlideToSafetyFallbackPlanARC(BundleContext context, String bundleId) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new GlideToSafetyFallbackPlan(context, bundleId));
	}
	
	public IGlideToSafetyFallbackPlan getTheGlideToSafetyFallbackPlanFlyingService() {
		return (IGlideToSafetyFallbackPlan) this.getTheFlyingService();
	}
	@Override
	public IAdaptiveReadyComponent bindService(String req, Object value) {
		if(req.equals(REQUIRED_RADIOALTIMETER)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setRadioAltimeter((IRadioAltimeterSensor) value);
		}else if(req.equals(REQUIRED_CONTROLSURFACES)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setControlSurfaces((IControlSurfaces) value);
		}else if(req.equals(REQUIRED_ATTITUDESENSOR)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setAttitudeSensor((IAttitudeSensor) value);

		} else if(req.equals(REQUIRED_ALTITUDESENSOR)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setAltitudeSensor((IAltitudeSensor) value);
		} else if(req.equals(REQUIRED_ETL)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setETLSensor((IETL) value);		
		} else if(req.equals(REQUIRED_SPEEDSENSOR)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setSpeedSensor((ISpeedSensor) value);
		} else if(req.equals(REQUIRED_PROXIMITYSENSOR)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setProximitySensor((IProximitySensor) value);
		} else {
			logger.error("Unknown service required: " + req);
		}
		
		return super.bindService(req, value);
	}

	@Override
	public IAdaptiveReadyComponent unbindService(String req, Object value) {
		if(req.equals(REQUIRED_RADIOALTIMETER)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setRadioAltimeter(null);
		} else if(req.equals(REQUIRED_CONTROLSURFACES)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setControlSurfaces(null);
		} else if(req.equals(REQUIRED_ATTITUDESENSOR)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setAttitudeSensor(null);
		} else if(req.equals(REQUIRED_ALTITUDESENSOR)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setAltitudeSensor(null);
		} else if(req.equals(REQUIRED_ETL)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setETLSensor(null);
		} else if(req.equals(REQUIRED_SPEEDSENSOR)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setSpeedSensor(null);
		} else if(req.equals(REQUIRED_PROXIMITYSENSOR)) {
			this.getTheGlideToSafetyFallbackPlanFlyingService().setProximitySensor(null);
		} else {
			logger.error("Unknown service required: " + req);
		}
		return super.unbindService(req, value);
	}
	
	@Override
	public Object getServiceSupply(String serviceSupply) {
		if (serviceSupply.equals(PROVIDED_FLYINGSERVICE))
			return this.getTheGlideToSafetyFallbackPlanFlyingService();
		
		return super.getServiceSupply(serviceSupply);
	}

}
