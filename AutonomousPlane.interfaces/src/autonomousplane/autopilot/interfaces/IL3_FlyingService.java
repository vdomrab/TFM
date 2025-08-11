package autonomousplane.autopilot.interfaces;

import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.IProximitySensor;

public interface IL3_FlyingService extends IL2_FlyingService {
	void setEGTSensor(IEGTSensor sensor);
	
	void setFuelSensor(IFuelSensor sensor);
	
	void setProximitySensor(IProximitySensor sensor);
	
	void setLandingSystem(ILandingSystem landingSystem);
	
	
	
}
