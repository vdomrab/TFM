package autonomousplane.devices.interfaces;

import autonomousplane.interfaces.EFlyingStages;

public interface INavigationSystem {

	double getTotalDistance();
	double getCurrentDistance();
	EFlyingStages getCurrentFlyghtStage();
	
	INavigationSystem setTotalDistance(double totalDistance);
	INavigationSystem setCurrentDistance(double currentDistance);

	INavigationSystem calcualteCurrentDistance(double speed);
	EFlyingStages calculateTheFlyingStage(double altitude, double currentDistance, double totalDistance, double pitch);
	INavigationSystem setCurrentFlyghtStage(EFlyingStages stage);
	
}
