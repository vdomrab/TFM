package autonomousplane.autopilot.interfaces;

import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.IWeatherSensor;

public interface IEmergencyLandingFallbackPlan extends IGlideToSafetyFallbackPlan {

	void setFuelSensor(IFuelSensor fuelSensor);
	
	void setFADEC(IFADEC fadec);
	
	void setLandingSystem(ILandingSystem landingSystem);
	
	void setWeatherSensor(IWeatherSensor weatherSensor);
	
}
