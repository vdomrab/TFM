package autonomousplane.infraestructure.autopilot;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFallbackPlan;
import autonomousplane.autopilot.interfaces.IL2_FlyingService;
import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.interaction.interfaces.INotificationService;

public abstract class L2_FlyingService extends L1_FlyingService implements IL2_FlyingService {
	protected ISpeedSensor speedSensor = null;
	protected IAltitudeSensor altimeterSensor = null;
	protected IRadioAltimeterSensor radioAltimeterSensor = null;
	protected INavigationSystem navigationSystem = null;
	protected IFADEC FADEC = null;
	protected IFallbackPlan fallbackPlan = null;
	protected INotificationService notificationService = null;
	protected IAOASensor aoaSensor = null;
	protected IWeatherSensor weatherSensor = null;
	public L2_FlyingService(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IL2_FlyingService.class.getName());
	}
	@Override
	public void setFADEC(IFADEC sensor) {
		this.FADEC = sensor;
		return;
	}
	@Override
	public void setAltimeterSensor(IAltitudeSensor sensor) {
		this.altimeterSensor = sensor;
		return;
	}
	@Override
	public void setRadioAltimeterSensor(IRadioAltimeterSensor sensor) {
		this.radioAltimeterSensor = sensor;
		return;
	}
	@Override
	public void setSpeedSensor(ISpeedSensor sensor) {
		this.speedSensor = sensor;
		return;
	}
	@Override
	public void setGNSS(INavigationSystem sensor) {
		this.navigationSystem = sensor;
		return;
	}
	public void setNotificationService(INotificationService service) {
		this.notificationService = service;
		return;
	}
	public INotificationService getNotificationService() {
		return this.notificationService;
	}
	
	public void setAOASensor(IAOASensor sensor) {
		this.aoaSensor = sensor;
		return;
	}

	public void setFallbackPlan(IFallbackPlan fallbackPlan) {
		this.fallbackPlan = fallbackPlan;
		return;
	}
	
	public void setWeatherSensor(IWeatherSensor sensor) {
		this.weatherSensor = sensor;
		return;
	}
	
	protected IAOASensor getAOASensor() {
		return this.aoaSensor;
	}
	public IFADEC getFADEC() {
		return this.FADEC;
	}
	
	public IWeatherSensor getWeatherSensor() {
		return this.weatherSensor; // This method is not implemented in this class
	}
	
	public IAltitudeSensor getAltimeterSensor() {
		return this.altimeterSensor;
	}
	public IRadioAltimeterSensor getRadioAltemeterSensor() {
		return this.radioAltimeterSensor;
	}
	public ISpeedSensor getSpeedSensor() {
		return this.speedSensor;
	}
	public INavigationSystem getGNSS() {
		return this.navigationSystem;
	}
	public IFallbackPlan getFallbackPlan() {
		return this.fallbackPlan;
	}
	
	public IL2_FlyingService performTheTakeOver() {
		this.endFlight();
		this.getNotificationService().notify("Exited Autonomous Mode");
		return this;
	}
	public IL2_FlyingService activateTheFallbackPlan() {
		this.endFlight();
		this.getFallbackPlan().startFlight();

		return this;
	}
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean result = true;
		
		if(this.getFADEC() == null) {
			
			result = false;
			logger.warn("The FADEC(ENGINE) is not set. The flying service cannot be performed.");
		}
		if(this.getAltimeterSensor() == null) {
			result = false;
			logger.warn("The AltimeterSensor is not set. The flying service cannot be performed.");
		}
		if(this.getRadioAltemeterSensor() == null) {
			result = false;
			logger.warn("The RadioAltimeterSensor is not set. The flying service cannot be performed.");
		}
		if(this.getSpeedSensor() == null) {
			result = false;
			logger.warn("The SpeedSensor is not set. The flying service cannot be performed.");
		}
		if(this.getGNSS() == null) {
			result = false;
			logger.warn("The GNSS NavigationSystem is not set. The flying service cannot be performed.");
		}
		if(this.getFallbackPlan() == null) {
			result = false;
			logger.warn("The FallbackPlan is not set. The flying service cannot be performed.");
		}
		
		if(this.getAOASensor() == null) {
			result = false;
			logger.warn("The AOA Sensor is not set. The flying service cannot be performed.");
		}
		
		if(this.getNotificationService() == null) {
			result = false;
			logger.warn("The NotificationService is not set. The flying service cannot be performed.");
		}
		if(this.getWeatherSensor() == null) {
			result = false;
			logger.warn("The WeatherSensor is not set. The flying service cannot be performed.");
		}
		return result && super.checkRequirementsToPerformTheFlyingService();
	}
}
