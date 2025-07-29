package autonomousplane.devices.interfaces;

public interface ILandingSystem {
	double getRunwayHeadingDegrees();
	ILandingSystem setRunwayHeadingDegrees(double getAngleDegrees);

	public boolean isAlignedWithRunway(double pitch);
}
