package autonomousplane.autopilot.interfaces;

import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.interaction.interfaces.INotificationService;

public interface IL2_FlyingService extends IL1_FlyingService{
	public void setFADEC(IFADEC sensor);
	public void setAltimeterSensor(IAltitudeSensor sensor);
	public void setRadioAltimeterSensor(IRadioAltimeterSensor sensor);
	public void setSpeedSensor(ISpeedSensor sensor);
	public void setGNSS(INavigationSystem sensor);
	public void setFallbackPlan(IFallbackPlan fallbackPlan);
	public void setNotificationService(INotificationService service);
	public void setAOASensor(IAOASensor sensor);
	public void setWeatherSensor(IWeatherSensor sensor);
	
	public IL2_FlyingService performTheTakeOver();
	public IL2_FlyingService activateTheFallbackPlan();
}
