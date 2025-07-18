package autonomousplane.autopilot.interfaces;

import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;

public interface IStallRecoveryFallbackPlan extends IFallbackPlan {

	public void setFADEC(IFADEC fadec);
	
	public void setControlSurface(IControlSurfaces controlSurface);
	
	public void setAttitudeSensor(IAttitudeSensor attitudeSensor);
	
	public void setSpeedSensor(ISpeedSensor speedSensor);
	
	public void setAltitudeSensor(IAltitudeSensor altitudeSensor);
	
	public void setAOASensor(IAOASensor aoaSensor);
	
	public void setWeatherSensor(IWeatherSensor weatherSensor);
}
