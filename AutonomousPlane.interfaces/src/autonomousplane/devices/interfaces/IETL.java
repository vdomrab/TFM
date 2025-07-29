package autonomousplane.devices.interfaces;

import autonomousplane.interfaces.EFlyingStages;

public interface IETL {
	
	public IETL sendSignalString( String emergencyCode,
    long timestamp,
    double altitude,
    double verticalSpeed,
    double pitch,
    double roll,
    double yaw,
    double airspeed,
    double groundSpeed,
    double angleOfAttack,
    double thrust,
    String destination,
    EFlyingStages phase);
	
	public boolean isSignalSent();
	
	public void resetSignalSent();

}
