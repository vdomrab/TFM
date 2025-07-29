package autonomousplane.infraestructure.autopilotARC;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFallbackPlan;
import autonomousplane.autopilot.interfaces.IL2_FlyingService;
import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.interaction.interfaces.INotificationService;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;

public class L2_FlyingServiceARC extends L1_FlyingServiceARC {

	public static String REQUIRED_FADEC = "required_FADEC";
	public static String REQUIRED_ALTIMETERSENSOR = "required_AltimeterSensor";
	public static String REQUIRED_RADIOALTIMETERSENSOR = "required_RadioAltimeterSensor";
	public static String REQUIRED_SPEEDSENSOR = "required_SpeedSensor";
	public static String REQUIRED_NAVIGATIONSYSTEM = "required_NavigationSystem";
	public static String REQUIERED_FALLBACKPLAN = "required_FallbackPlan";
	public static String REQUIRED_AOASENSOR = "required_AOASensor";
	public static String REQUIRED_NOTIFICATIONSERVICE = "required_notificationservice";
	public static String REQUIERED_WEATHERSENSOR = "required_WeatherSensor";
	
	public L2_FlyingServiceARC(BundleContext context, String bundleId) {
		super(context, bundleId);
	}

	protected IL2_FlyingService getTheL2FlyingService() {
		return (IL2_FlyingService) this.getTheFlyingService();
	}
	@Override
	public IAdaptiveReadyComponent bindService(String req, Object value) {
		if (req.equals(REQUIRED_FADEC)) {
			this.getTheL2FlyingService().setFADEC((IFADEC) value);
		}
		else if (req.equals(REQUIRED_ALTIMETERSENSOR)) {
			this.getTheL2FlyingService().setAltimeterSensor((IAltitudeSensor) value);
		}
		else if (req.equals(REQUIRED_RADIOALTIMETERSENSOR)) {
			this.getTheL2FlyingService().setRadioAltimeterSensor((IRadioAltimeterSensor) value);
		}
		else if (req.equals(REQUIRED_SPEEDSENSOR)) {
			this.getTheL2FlyingService().setSpeedSensor((ISpeedSensor) value);	
		}
		else if (req.equals(REQUIRED_NAVIGATIONSYSTEM)) {
			this.getTheL2FlyingService().setNavigationSystem((INavigationSystem) value);
		}else if (req.equals(REQUIERED_FALLBACKPLAN)) {
			this.getTheL2FlyingService().setFallbackPlan((IFallbackPlan) value);
		}else if (req.equals(REQUIRED_AOASENSOR))
			this.getTheL2FlyingService().setAOASensor((IAOASensor)value);	
		else if (req.equals(REQUIRED_NOTIFICATIONSERVICE))
			this.getTheL2FlyingService().setNotificationService((INotificationService)value);
		else if(req.equals(REQUIERED_WEATHERSENSOR)) {
			this.getTheL2FlyingService().setWeatherSensor((IWeatherSensor)value);
		}
		
		return super.bindService(req, value);
	}
	
	@Override
	public IAdaptiveReadyComponent unbindService(String req, Object value) {
		if (req.equals(REQUIRED_FADEC)) {
			this.getTheL2FlyingService().setFADEC(null);
		}
		else if (req.equals(REQUIRED_ALTIMETERSENSOR)) {
			this.getTheL2FlyingService().setAltimeterSensor(null);
		}
		else if (req.equals(REQUIRED_RADIOALTIMETERSENSOR)) {
			this.getTheL2FlyingService().setRadioAltimeterSensor(null);		
		}
		else if (req.equals(REQUIRED_SPEEDSENSOR)) {
			this.getTheL2FlyingService().setSpeedSensor(null);	
		}
		else if (req.equals(REQUIRED_NAVIGATIONSYSTEM)) {
			this.getTheL2FlyingService().setNavigationSystem(null);
		} else if (req.equals(REQUIERED_FALLBACKPLAN)) {
			this.getTheL2FlyingService().setFallbackPlan(null);
		}else if (req.equals(REQUIRED_AOASENSOR)) {
			this.getTheL2FlyingService().setAOASensor(null);
		} else if (req.equals(REQUIRED_NOTIFICATIONSERVICE)) {
			this.getTheL2FlyingService().setNotificationService(null);
		}
		return super.unbindService(req, value);
	}
}
