package autonomousplane.devices.interfaces;


public interface IRadioAltimeterSensor {

	public IRadioAltimeterSensor setGroundDistance(double angleOfAttack);
	
	public double getGroundDistance();
	boolean isOnGround();
	
	public IRadioAltimeterSensor setRealGroundAltitude(double groundAltitude);
    double getRealGroundAltitude();

}	
