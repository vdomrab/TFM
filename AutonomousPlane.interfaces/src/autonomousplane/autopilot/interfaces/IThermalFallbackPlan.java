package autonomousplane.autopilot.interfaces;

import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IWeatherSensor;

public interface IThermalFallbackPlan extends IFallbackPlan {

	public void setFADEC(IFADEC fadec);
	
	public void setEGTSensor(IEGTSensor egtSensor);
	
	public void setWeatherSensor(IWeatherSensor weatherSensor);

}
