package autopilot.emergencylandingfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IEmergencyLandingFallbackPlan;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IETL;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.autopilotARC.FallbackPlanARC;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.impl.AdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class EmergencyLandingFallbackPlanARC extends FallbackPlanARC {

	public static String REQUIRED_RADIOALTIMETER = "required_radioaltimeter";
	public static String REQUIRED_CONTROLSURFACES = "required_controlsurfaces";
	public static String REQUIRED_ATTITUDESENSOR = "required_attitudesensor";
	public static String REQUIRED_ALTITUDESENSOR = "required_altitudesensor";
	public static String REQUIRED_ETL = "required_etl";
	public static String REQUIRED_SPEEDSENSOR = "required_speedsensor";
	public static String REQUIRED_PROXIMITYSENSOR = "required_proximitysensor";
	public static String REQUIRED_FADEC = "required_fadec";
	public static String REQUIRED_LANDINGSYSTEM = "required_landingsystem";
	public static String REQUIRED_FUELSENSOR = "required_fuelsensor";
	public static String REQUIRED_WEATHERSENSOR = "required_weathersensor";
	public EmergencyLandingFallbackPlanARC(BundleContext context, String bundleName) {
		super(context, bundleName);
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new EmergencyLandingFallbackPlan(context, bundleName));
	}

	public IEmergencyLandingFallbackPlan getTheEmergencyLandingFallbackPlanFlyingService() {
		return (IEmergencyLandingFallbackPlan) this.getTheFlyingService();
	}
	
	public IAdaptiveReadyComponent bindService(String req, Object value) {
		if (req.equals(REQUIRED_RADIOALTIMETER)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setRadioAltimeter((IRadioAltimeterSensor) value);
		} else if (req.equals(REQUIRED_CONTROLSURFACES)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setControlSurfaces((IControlSurfaces) value);
		} else if (req.equals(REQUIRED_ATTITUDESENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setAttitudeSensor((IAttitudeSensor) value);
		} else if (req.equals(REQUIRED_ALTITUDESENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setAltitudeSensor((IAltitudeSensor) value);
		} else if (req.equals(REQUIRED_ETL)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setETLSensor((IETL) value);
		} else if (req.equals(REQUIRED_SPEEDSENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setSpeedSensor((ISpeedSensor) value);
		} else if (req.equals(REQUIRED_PROXIMITYSENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setProximitySensor((IProximitySensor) value);
		} else if (req.equals(REQUIRED_FADEC)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setFADEC((IFADEC) value);
		} else if (req.equals(REQUIRED_LANDINGSYSTEM)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setLandingSystem((ILandingSystem) value);
		} else if (req.equals(REQUIRED_FUELSENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setFuelSensor((IFuelSensor) value);
		} else if (req.equals(REQUIRED_WEATHERSENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setWeatherSensor((IWeatherSensor) value);
		} else {
			logger.error("Unknown service required: " + req);
		}
		
		return super.bindService(req, value);
	}
	
	public IAdaptiveReadyComponent unbindService(String req, Object value) {
		if (req.equals(REQUIRED_RADIOALTIMETER)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setRadioAltimeter(null);
		} else if (req.equals(REQUIRED_CONTROLSURFACES)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setControlSurfaces(null);
		} else if (req.equals(REQUIRED_ATTITUDESENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setAttitudeSensor(null);
		} else if (req.equals(REQUIRED_ALTITUDESENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setAltitudeSensor(null);
		} else if (req.equals(REQUIRED_ETL)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setETLSensor(null);
		} else if (req.equals(REQUIRED_SPEEDSENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setSpeedSensor(null);
		} else if (req.equals(REQUIRED_PROXIMITYSENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setProximitySensor(null);
		} else if (req.equals(REQUIRED_FADEC)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setFADEC(null);
		} else if (req.equals(REQUIRED_LANDINGSYSTEM)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setLandingSystem(null);
		} else if (req.equals(REQUIRED_FUELSENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setFuelSensor(null);
		} else if (req.equals(REQUIRED_WEATHERSENSOR)) {
			this.getTheEmergencyLandingFallbackPlanFlyingService().setWeatherSensor(null);
		} else {
			logger.error("Unknown service required: " + req);
		}
		
		return super.unbindService(req, value);
	}
	
	@Override
	public Object getServiceSupply(String serviceSupply) {
		if (serviceSupply.equals(PROVIDED_FLYINGSERVICE))
			return this.getTheEmergencyLandingFallbackPlanFlyingService();
		
		return super.getServiceSupply(serviceSupply);
	}

}
