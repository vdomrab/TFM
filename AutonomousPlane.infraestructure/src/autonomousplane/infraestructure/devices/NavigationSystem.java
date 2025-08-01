package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.infraestructure.Thing;
import autonomousplane.interfaces.EFlyingStages;

public class NavigationSystem extends Thing implements INavigationSystem{

	public static final String TOTAL_DISTANCE = "total_distance";
	public static final String CURRENT_DISTANCE = "current_distance";
	public static final String CURRENT_FLYGHT_STAGE = "current_flyght_stage";
	public static final double MAX_DISTANCE = 1000000.0; // Maximum total distance in meters
	public static final double MIN_DISTANCE = 0.0; // Minimum total distance in meters

	public NavigationSystem(BundleContext context,String id) {
		super(context, id);
		this.addImplementedInterface(INavigationSystem.class.getName());
		this.setTotalDistance(500000);
		this.setCurrentDistance(0.0);
		this.setCurrentFlyghtStage(EFlyingStages.TAKEOFF);
	}

	@Override
	public double getTotalDistance() {
		return (double) this.getProperty(TOTAL_DISTANCE);
	}

	@Override
	public double getCurrentDistance() {
		return (double) this.getProperty(CURRENT_DISTANCE);
	}
		@Override
	public INavigationSystem setTotalDistance(double totalDistance) {
		double clampedTotalDistance = Math.max(MIN_DISTANCE, Math.min(totalDistance, MAX_DISTANCE));
		this.setProperty(TOTAL_DISTANCE, clampedTotalDistance);
		return this;
	}

	@Override
	public INavigationSystem setCurrentDistance(double currentDistance) {
		double clampedCurrentDistance = Math.max(MIN_DISTANCE, Math.min(currentDistance, this.getTotalDistance()));
		this.setProperty(CURRENT_DISTANCE, clampedCurrentDistance);
		return this;
	}
	


	@Override
	public INavigationSystem calcualteCurrentDistance(double speed) {
		double msSPeed = speed; // Convert km/h to m/s
		double currentDistance = this.getCurrentDistance() + msSPeed;
		this.setCurrentDistance(currentDistance);
		return this;
	}

	@Override
	public INavigationSystem setCurrentFlyghtStage(EFlyingStages stage) {
		this.setProperty(CURRENT_FLYGHT_STAGE, stage);
		return this;
	}

	@Override
	public EFlyingStages getCurrentFlyghtStage() {
		return (EFlyingStages) this.getProperty(CURRENT_FLYGHT_STAGE);
	}
	
	public EFlyingStages calculateTheFlyingStage(double altitude) {
		double verticalSpeed = -5.0; // Placeholder for actual vertical speed, should be set based on real data
	    final double TAKEOFF_DISTANCE_THRESHOLD = 2500.0;   // meters
	    final double CLIMB_START_DISTANCE = 2500.0;
	    final double LANDING_ALTITUDE_THRESHOLD = 600.0;    // meters (updated as per your suggestion)

	    double currentDistance = this.getCurrentDistance();
	    double totalDistance = this.getTotalDistance();
	    double horizontalDistanceRemaining = totalDistance - currentDistance;

	    EFlyingStages currentStage = this.getCurrentFlyghtStage();

	    // Use vertical speed to estimate descent distance
	    double timeToDescend = (altitude + LANDING_ALTITUDE_THRESHOLD + 200) / Math.abs(verticalSpeed); // seconds
	    double averageGroundSpeed = 94; 
	    double requiredDescentDistance = averageGroundSpeed * timeToDescend;
	    System.out.println("Required descent distance: " + requiredDescentDistance + " meters");
	    System.out.println("Current distance: " + currentDistance + " meters");
	    System.out.println("Remaining distance: " + horizontalDistanceRemaining + " meters");
	    System.out.println("currentDistance >= CLIMB_START_DISTANCE: " + (currentDistance >= CLIMB_START_DISTANCE));
	    System.out.println("altitude <= 9000: " + (altitude <= 9000));
	    System.out.println("getCurrentFlyghtStage() == EFlyingStages.TAKEOFF: " + (this.getCurrentFlyghtStage() == EFlyingStages.TAKEOFF));
	    System.out.println("horizontalDistanceRemaining > requiredDescentDistance: " + (horizontalDistanceRemaining > requiredDescentDistance));
	    // --- Stable phase logic ---
	    if (currentStage == EFlyingStages.DESCENT || currentStage == EFlyingStages.LANDING) {
	        if (altitude <= LANDING_ALTITUDE_THRESHOLD && currentDistance > 0 && horizontalDistanceRemaining <= 5000) {
	            return EFlyingStages.LANDING;
	        }
	        return EFlyingStages.DESCENT;
	    }

	    if (currentDistance < TAKEOFF_DISTANCE_THRESHOLD) {
	        return EFlyingStages.TAKEOFF;
	    }
	    
	    if (currentDistance >= CLIMB_START_DISTANCE && altitude <= 9000 && this.getCurrentFlyghtStage() == EFlyingStages.TAKEOFF && horizontalDistanceRemaining > requiredDescentDistance) {
	        return EFlyingStages.CLIMB;
	    }

	    if (horizontalDistanceRemaining > requiredDescentDistance && altitude > 9000) {
	        return EFlyingStages.CRUISE;
	    }

	    // Start descent when needed
	    if (horizontalDistanceRemaining <= requiredDescentDistance && altitude > LANDING_ALTITUDE_THRESHOLD) {
	        return EFlyingStages.DESCENT;
	    }

	    // Fallback
	    return currentStage;
	}



}
