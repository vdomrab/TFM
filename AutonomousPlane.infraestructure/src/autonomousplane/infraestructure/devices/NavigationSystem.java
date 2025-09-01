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
	private boolean descentStarted = false;             // ¿Se inició la maniobra de descenso?
	private double descentStartHorizontalDistance = 0; // Distancia horizontal cuando se inicia la maniobra
	private double estimatedEstablishmentDistance = 0; // Distancia estimada para alcanzar tasa vertical objetivo
	private double previousAltitude = 0;
	private double previousVerticalSpeed = 0;
	private double previousHorizontalDistance = 0;
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
	
	
	public EFlyingStages calculateTheFlyingStage(double altitude, double currentDistance, double totalDistance, double pitch) {
	    double verticalSpeed = -5.0; // como placeholder

	    double horizontalDistanceRemaining = totalDistance - currentDistance;
	    EFlyingStages currentStage = this.getCurrentFlyghtStage();
	    // Distancia necesaria para descender según altitud y velocidad vertical
	    double timeToDescend = (altitude) / Math.abs(verticalSpeed);
	    double averageGroundSpeed = 96.87143895477621; 
	    double requiredDescentDistance = (averageGroundSpeed * timeToDescend) ;
	    final double TAKEOFF_DISTANCE_THRESHOLD = 2500.0;   // meters
	    final double CLIMB_START_DISTANCE = 2500.0;
	
	    // Llamar para actualizar la estimación del delay dinámico
	    updateDescentEstimation(altitude, currentDistance, pitch);

	    // Sumamos la distancia extra que tarda en establecer la tasa de descenso
	    double adjustedRequiredDistance = requiredDescentDistance + estimatedEstablishmentDistance;

	    // Lógica para decidir la fase
	    if (currentStage == EFlyingStages.DESCENT || currentStage == EFlyingStages.LANDING) {
	        if (currentDistance > 0 && horizontalDistanceRemaining <= 5000) {
	            return EFlyingStages.LANDING;
	        }
	        return EFlyingStages.DESCENT;
	    }

	    if (currentDistance < TAKEOFF_DISTANCE_THRESHOLD) {
	        return EFlyingStages.TAKEOFF;
	    }

	    if (currentDistance >= CLIMB_START_DISTANCE && altitude <= 9000 && this.getCurrentFlyghtStage() == EFlyingStages.TAKEOFF && horizontalDistanceRemaining > adjustedRequiredDistance) {
	        return EFlyingStages.CLIMB;
	    }

	    if (horizontalDistanceRemaining > adjustedRequiredDistance && altitude > 9000) {
	        return EFlyingStages.CRUISE;
	    }

	    // Iniciar descenso solo si queda menos distancia que la ajustada (con delay dinámico)
	    if (horizontalDistanceRemaining <= adjustedRequiredDistance) {
	        return EFlyingStages.DESCENT;
	    }

	    return currentStage;
	}

	public void updateDescentEstimation(double currentAltitude, double currentHorizontalDistance, double pitch) {
	    double deltaDistance = currentHorizontalDistance - previousHorizontalDistance;
	    if (deltaDistance <= 0) return; // evitar valores erróneos

	    // Velocidad vertical estimada (m/s)
	    double verticalSpeed = (currentAltitude - previousAltitude) / (deltaDistance / 94.0); // v_x=94 m/s

	    // Aceleración vertical estimada (m/s²)
	    double verticalAcceleration = (verticalSpeed - previousVerticalSpeed) / (deltaDistance / 94.0);

	    // Detectar inicio de maniobra descendente por pitch negativo
	    if (!descentStarted && pitch < -1.0) {
	        descentStarted = true;
	        descentStartHorizontalDistance = currentHorizontalDistance;

	        double vy0 = verticalSpeed;
	        double targetVy = -5.0;
	        double ay = Math.abs(verticalAcceleration);

	        if (ay > 0.01) {
	            double tEst = Math.abs(targetVy - vy0) / ay;  // tiempo en segundos
	            estimatedEstablishmentDistance = 94.0 * tEst; // distancia horizontal para alcanzar tasa de descenso
	        } else {
	            estimatedEstablishmentDistance = 0; // si aceleración muy baja, asumir sin delay
	        }
	    }

	    previousAltitude = currentAltitude;
	    previousVerticalSpeed = verticalSpeed;
	    previousHorizontalDistance = currentHorizontalDistance;
	}


}
