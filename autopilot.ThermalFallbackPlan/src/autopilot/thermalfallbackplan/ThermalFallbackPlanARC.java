package autopilot.thermalfallbackplan;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IThermalFallbackPlan;
import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.autopilotARC.FallbackPlanARC;
import autonomousplane.interaction.interfaces.INotificationService;

public class ThermalFallbackPlanARC extends FallbackPlanARC {
	public static String REQUIRED_FADEC = "required_fadec";
	public static String REQUIRED_WEATHERSENSOR = "required_weathersensor";
	public static String REQUIRED_EGTSENSOR = "required_egtsensor";
	public static String REQUIRED_NOTIFICATIONSERVICE = "required_notificationservice";

	public ThermalFallbackPlanARC(BundleContext context, String bundleId) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new ThermalFallbackPlan(this.context, bundleId));
	}
	
	public IThermalFallbackPlan getTheThermalFallbackPlanFlyingService() {
		return (IThermalFallbackPlan) this.getTheFlyingService();
	}
	@Override
	public IAdaptiveReadyComponent bindService(String req, Object value) {
		if (req.equals(REQUIRED_FADEC)) {
			this.getTheThermalFallbackPlanFlyingService().setFADEC((IFADEC) value);
		} else if (req.equals(REQUIRED_WEATHERSENSOR)) {
			this.getTheThermalFallbackPlanFlyingService().setWeatherSensor((IWeatherSensor) value);
		} else if (req.equals(REQUIRED_EGTSENSOR)) {
			this.getTheThermalFallbackPlanFlyingService().setEGTSensor((IEGTSensor) value);
		} else if (req.equals(REQUIRED_NOTIFICATIONSERVICE)) {
			this.getTheThermalFallbackPlanFlyingService().setNotificationService((INotificationService) value);
		} else {
			logger.error("Unknown service required: " + req);
		}
		
		return super.bindService(req, value);
	}
	
	@Override
	public IAdaptiveReadyComponent unbindService(String req, Object value) {
		if (req.equals(REQUIRED_FADEC)) {
			this.getTheThermalFallbackPlanFlyingService().setFADEC(null);
		} else if (req.equals(REQUIRED_WEATHERSENSOR)) {
			this.getTheThermalFallbackPlanFlyingService().setWeatherSensor(null);
		} else if (req.equals(REQUIRED_EGTSENSOR)) {
			this.getTheThermalFallbackPlanFlyingService().setEGTSensor(null);
		} else if (req.equals(REQUIRED_NOTIFICATIONSERVICE)) {
			this.getTheThermalFallbackPlanFlyingService().setNotificationService(null);
		} else {
			logger.error("Unknown service required: " + req);
		}
		
		return super.unbindService(req, value);
	}

	@Override
	public Object getServiceSupply(String serviceSupply) {
		if (serviceSupply.equals(PROVIDED_FLYINGSERVICE))
			return this.getTheThermalFallbackPlanFlyingService();
		
		return super.getServiceSupply(serviceSupply);
	}
}
