package autonomousplane.autopilot.interfaces;

import autonomousplane.interfaces.IIdentifiable;

public interface IFlyingService extends IIdentifiable {

	public IFlyingService startFlight();
	public IFlyingService endFlight();
	public boolean isFlying();
}
