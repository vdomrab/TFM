package autonomousplane.autopilot.interfaces;

import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IETL;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;

public interface IGlideToSafetyFallbackPlan extends IFallbackPlan {

	void setRadioAltimeter(IRadioAltimeterSensor radioAltimeter);
	
	void setControlSurfaces(IControlSurfaces controlSurfaces);
	
	void setAttitudeSensor(IAttitudeSensor attitudeSensor);
	
	void setAltitudeSensor(IAltitudeSensor altitudeSensor);
	
	void setETLSensor(IETL etlSensor);
	
	void setSpeedSensor(ISpeedSensor speedSensor);
	
	void setProximitySensor(IProximitySensor lidarSensor);


}
