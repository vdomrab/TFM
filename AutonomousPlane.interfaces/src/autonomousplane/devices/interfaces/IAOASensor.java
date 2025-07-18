package autonomousplane.devices.interfaces;

public interface IAOASensor {

	public IAOASensor setAOA(double angleOfAttack);
	public double calculateAOA(double pitchDegrees, double verticalSpeed, double horizontalSpeed);
	public double getAOA();
}
