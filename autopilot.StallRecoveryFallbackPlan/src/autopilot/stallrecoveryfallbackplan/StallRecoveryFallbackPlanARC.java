package autopilot.stallrecoveryfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IStallRecoveryFallbackPlan;
import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.autopilotARC.FallbackPlanARC;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;


public class StallRecoveryFallbackPlanARC extends FallbackPlanARC {
	public static String REQUIRED_FADEC = "required_fadec";
	public static String REQUIRED_CONTROLSURFACE = "required_controlsurface";
	public static String REQUIRED_ATTITUDESENSOR = "required_attitudesensor";
	public static String REQUIRED_SPEEDSENSOR = "required_speedsensor";
	public static String REQUIRED_ALTITUDESENSOR = "required_altitudesensor";
	public static String REQUIRED_AOASENSOR = "required_aoasensor";
	public static String REQUIRED_WEATHERSENSOR = "required_weathersensor";
	public static String REQUIRED_NOTIFICATIONSERVICE = "required_notificationservice";

	public StallRecoveryFallbackPlanARC(BundleContext context, String bundleId) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new StallRecoveryFallbackPlan(this.context, bundleId));
	}


	protected IStallRecoveryFallbackPlan getTheStallRecoveryFallbackPlanFlyingService() {
		return (IStallRecoveryFallbackPlan) this.getTheFlyingService();
	}
	@Override
	public IAdaptiveReadyComponent bindService(String req, Object value) {
		if (req.equals(REQUIRED_FADEC))
			this.getTheStallRecoveryFallbackPlanFlyingService().setFADEC((IFADEC) value);
		else if (req.equals(REQUIRED_CONTROLSURFACE))
			this.getTheStallRecoveryFallbackPlanFlyingService().setControlSurface((IControlSurfaces) value);
		else if (req.equals(REQUIRED_ATTITUDESENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setAttitudeSensor((IAttitudeSensor) value);
		else if (req.equals(REQUIRED_SPEEDSENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setSpeedSensor((ISpeedSensor) value);
		else if (req.equals(REQUIRED_ALTITUDESENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setAltitudeSensor((IAltitudeSensor) value);
		else if (req.equals(REQUIRED_AOASENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setAOASensor((IAOASensor) value);
		else if (req.equals(REQUIRED_WEATHERSENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setWeatherSensor((IWeatherSensor) value); // Assuming weather sensor is a generic object
		else if (req.equals(REQUIRED_NOTIFICATIONSERVICE))
			this.getTheStallRecoveryFallbackPlanFlyingService().setNotificationService((autonomousplane.interaction.interfaces.INotificationService) value);
		else
			logger.error("Unknown service required: " + req);
		
		return super.bindService(req, value);
	}

	@Override
	public IAdaptiveReadyComponent unbindService(String req, Object value) {
		if (req.equals(REQUIRED_FADEC))
			this.getTheStallRecoveryFallbackPlanFlyingService().setFADEC(null);
		else if (req.equals(REQUIRED_CONTROLSURFACE))
			this.getTheStallRecoveryFallbackPlanFlyingService().setControlSurface(null);
		else if (req.equals(REQUIRED_ATTITUDESENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setAttitudeSensor(null);
		else if (req.equals(REQUIRED_SPEEDSENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setSpeedSensor(null);
		else if (req.equals(REQUIRED_ALTITUDESENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setAltitudeSensor(null);
		else if (req.equals(REQUIRED_AOASENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setAOASensor(null);
		else if (req.equals(REQUIRED_WEATHERSENSOR))
			this.getTheStallRecoveryFallbackPlanFlyingService().setWeatherSensor(null);
		else if (req.equals(REQUIRED_NOTIFICATIONSERVICE))
			this.getTheStallRecoveryFallbackPlanFlyingService().setNotificationService(null);
		else
			logger.error("Unknown service required: " + req);
		
		return super.unbindService(req, value);
	}

		
	@Override
	public Object getServiceSupply(String serviceSupply) {
		if (serviceSupply.equals(PROVIDED_FLYINGSERVICE))
			return this.getTheStallRecoveryFallbackPlanFlyingService();
		
		return super.getServiceSupply(serviceSupply);
	}
}
