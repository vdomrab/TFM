package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.infraestructure.Thing;

public class ControlSurfaces extends Thing implements IControlSurfaces {

	public static final String AILERON_DEFLECTION = "aileronDeflection";
	public static final String ELEVATOR_DEFLECTION = "elevatorDeflection";
	public static final String RUDDER_DEFLECTION = "rudderDeflection";
	public static final String AIRBRAKE_DEPLOYMENT = "airbrakeDeployment";

	public static final double MAX_AILERON_DEFLECTION = 20.0;
	public static final double MAX_ELEVATOR_DEFLECTION = 15.0;
	public static final double MAX_RUDDER_DEFLECTION = 25.0;
	public static final double MAX_AIRBRAKE_DEPLOYMENT = 1.0; // 0.0 (retraído) a 1.0 (totalmente extendido)

	public ControlSurfaces(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IControlSurfaces.class.getName());
		this.setAileronDeflection(0.0);
		this.setElevatorDeflection(0.0);
		this.setRudderDeflection(0.0);
		this.setAirbrakeDeployment(0.0); 
	}

	@Override
	public void setAileronDeflection(double deflection) {
		this.setProperty(AILERON_DEFLECTION, clampDeflection(deflection, MAX_AILERON_DEFLECTION, -MAX_AILERON_DEFLECTION));
	}

	@Override
	public void setElevatorDeflection(double deflection) {
		this.setProperty(ELEVATOR_DEFLECTION, clampDeflection(deflection, MAX_ELEVATOR_DEFLECTION, -MAX_ELEVATOR_DEFLECTION));
	}

	@Override
	public void setRudderDeflection(double deflection) {
		this.setProperty(RUDDER_DEFLECTION, clampDeflection(deflection, MAX_RUDDER_DEFLECTION, -MAX_RUDDER_DEFLECTION));
	}
	@Override
	public void setAirbrakeDeployment(double deployment) {
	    this.setProperty(AIRBRAKE_DEPLOYMENT, clampDeployment(deployment));
	}


	@Override
	public double getAileronDeflection() {
		return (double) this.getProperty(AILERON_DEFLECTION);
	}

	@Override
	public double getElevatorDeflection() {
		return (double) this.getProperty(ELEVATOR_DEFLECTION);
	}

	@Override
	public double getRudderDeflection() {
		return (double) this.getProperty(RUDDER_DEFLECTION);
	}
	
	
	public double getAirbrakeDeployment() {
	    return (double) this.getProperty(AIRBRAKE_DEPLOYMENT);
	}

	private double clampDeployment(double value) {
	    return Math.max(0.0, Math.min(MAX_AIRBRAKE_DEPLOYMENT, value)); // sólo entre 0.0 y 1.0
	}
	
	private double clampDeflection(double deflection, double maxDeflection, double minDeflection) {
	        return Math.max(minDeflection, Math.min(maxDeflection, deflection)); // por ejemplo: -30° a +30° máx.
	}

	
}
