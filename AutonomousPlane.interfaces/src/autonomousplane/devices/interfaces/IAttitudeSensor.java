package autonomousplane.devices.interfaces;

public interface IAttitudeSensor {
	double getRoll();
	double getPitch();
	double getYaw();
	double getRollRate();
	double getPitchRate();
	double getYawRate();
		
	IAttitudeSensor setRoll(double roll);
	IAttitudeSensor setPitch(double pitch);
	IAttitudeSensor setYaw(double yaw);
	
	IAttitudeSensor updateAngularRates(double rollRate, double pitchRate, double yawRate);
	
	double calculateDensityFactor(IWeatherSensor weatherSensor);
	
	// Additional methods can be added as needed

}
