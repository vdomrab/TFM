package autonomousplane.infraestructure.devices;

import autonomousplane.infraestructure.Thing;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IAOASensor;
public class AOASensor extends Thing implements IAOASensor {

	public static final String AOA = "angleOfAttack";
	public static final double MAX_AOA = 20.0; 
	public static final double MIN_AOA = -10.0; // Minimum AOA is -10 degrees
	public AOASensor(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IAOASensor.class.getName());
		this.setAOA(0.0); // Initialize AOA to 0 degrees
	}

	@Override
	public double getAOA() {
		return (double) this.getProperty(AOASensor.AOA);
	}
	@Override
	public IAOASensor setAOA(double angleOfAttack) {
		double clampedAOA = Math.max(MIN_AOA, Math.min(angleOfAttack, AOASensor.MAX_AOA));
		this.setProperty(AOASensor.AOA, clampedAOA);
		return this;
	}
	
	
	public double calculateAOA(double vx, double vz, double pitchDegrees) {
	    double pitchRad = Math.toRadians(pitchDegrees);

	    // Ángulo de trayectoria de vuelo (Flight Path Angle)
	    double flightPathAngle = Math.atan2(vz, vx); // en radianes

	    // AOA = Pitch - Flight Path Angle
	    double aoaRad = pitchRad - flightPathAngle;
	    
	    return Math.toDegrees(aoaRad); // devolvemos el AOA en grados
	}

}
