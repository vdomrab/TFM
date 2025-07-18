package autonomousplane.devices.interfaces;

public interface ISpeedSensor {
	public double getSpeedTAS();
	public double getSpeedGS();
	public double getSpeedIncreaseTAS();

	public ISpeedSensor setSpeedTAS(double speed);
    public ISpeedSensor setSpeedIncreaseTAS(double speed);
    public ISpeedSensor setSpeedGS(double targetSpeed);
    
    public double calcualteSpeedIncreaseTAS(double thrust, double airDensity, double airBrakeLevel, double pitchDegrees, boolean isOnGround, double altitude);
    
    public double calculateGroundSpeed(double tas, double windSpeed, double windAngleDegrees);
}
