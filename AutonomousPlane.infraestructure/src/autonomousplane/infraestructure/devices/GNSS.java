package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.infraestructure.Thing;
import autonomousplane.interfaces.EFlyingStages;

public class GNSS extends Thing implements INavigationSystem{

	public static final String TOTAL_DISTANCE = "total_distance";
	public static final String CURRENT_DISTANCE = "current_distance";
	public static final String CURRENT_FLYGHT_STAGE = "current_flyght_stage";
	public static final double MAX_DISTANCE = 1000000.0; // Maximum total distance in meters
	public static final double MIN_DISTANCE = 0.0; // Minimum total distance in meters
	
	public GNSS(BundleContext context,String id) {
		super(context, id);
		this.addImplementedInterface(INavigationSystem.class.getName());
		this.setTotalDistance(0.0);
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
		double msSPeed = speed / 3.6; // Convert km/h to m/s
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
	    final double TAKEOFF_DISTANCE_THRESHOLD = 1000;   // metros
	    final double CLIMB_START_DISTANCE = 1500;
	    final double CLIMB_END_DISTANCE = 5000;
	    final double LANDING_ALTITUDE_THRESHOLD = 0;

	    double currentDistance = this.getCurrentDistance();
	    double totalDistance = this.getTotalDistance();
	    double cruiseDistanceEnd = totalDistance * 0.8;   // último 20% no crucero (descenso+aterrizaje)
	    
	    // Fase Takeoff: primeros 1000m
	    if (currentDistance < TAKEOFF_DISTANCE_THRESHOLD) {
	        return EFlyingStages.TAKEOFF;
	    }

	    // Fase Climb: entre 1000m y 5000m y altitud por debajo del crucero
	    if (currentDistance >= CLIMB_START_DISTANCE && currentDistance < CLIMB_END_DISTANCE ) {
	        return EFlyingStages.CLIMB;
	    }

	    // Fase Cruise: entre 20% y 80% de la distancia total y altitud en crucero o cerca
	    if (currentDistance >= CLIMB_END_DISTANCE && currentDistance <= cruiseDistanceEnd  ) {
	        return EFlyingStages.CRUISE;
	    }

	    // Fase Descent: después del 80% de la distancia y altitud bajando del crucero
	    if (currentDistance > cruiseDistanceEnd && altitude > LANDING_ALTITUDE_THRESHOLD) {
	        return EFlyingStages.DESCENT;
	    }

	    // Fase Landing: altitud 0 o cerca y distancia positiva (en tierra o muy cerca)
	    if (altitude <= LANDING_ALTITUDE_THRESHOLD && currentDistance > 0) {
	        return EFlyingStages.LANDING;
	    }
	    System.out.println("No se ha podido determinar la fase de vuelo, se retorna TAKEOFF por defecto.");
	    // Si no encaja en ninguna, por seguridad retornamos TAKEOFF (o podrías lanzar excepción)
	    return EFlyingStages.TAKEOFF;
	}
}
