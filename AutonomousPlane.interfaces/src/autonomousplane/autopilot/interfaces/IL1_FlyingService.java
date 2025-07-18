package autonomousplane.autopilot.interfaces;

import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;

public interface IL1_FlyingService extends IL0_FlyingService{
	public void setControlSurfaces(IControlSurfaces controlSurfaces);
	public void setAHRSSensor(IAttitudeSensor sensor);
	public void setStabilityModeActive(boolean isActive);
}
