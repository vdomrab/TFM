package autopilot.glidetosafetyfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IGlideToSafetyFallbackPlan;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IETL;
import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.infraestructure.autopilot.FallbackPlan;
import autonomousplane.simulation.simulator.PlaneSimulationElement;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class GlideToSafetyFallbackPlan extends FallbackPlan implements IGlideToSafetyFallbackPlan {
	// Attributes for the glide to safety fallback plan
	private IRadioAltimeterSensor radioAltimeter = null;
	private IControlSurfaces controlSurfaces = null;
	private IAttitudeSensor attitudeSensor = null;
	private IAltitudeSensor altitudeSensor = null;
	private IETL etlSensor = null;
	private ISpeedSensor speedSensor = null;
	private IProximitySensor proximitySensor = null;
	private INavigationSystem navigationSystem = null;
	public GlideToSafetyFallbackPlan(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.addImplementedInterface(IGlideToSafetyFallbackPlan.class.getName());
	}

	@Override
	public void setRadioAltimeter(IRadioAltimeterSensor radioAltimeter) {
		this.radioAltimeter = radioAltimeter;
	}

	@Override
	public void setControlSurfaces(IControlSurfaces controlSurfaces) {
		this.controlSurfaces = controlSurfaces;
	}
	@Override
	public void setNavigationSystem(INavigationSystem navigationSystem) {
		this.navigationSystem = navigationSystem;
	}
	@Override
	public void setAttitudeSensor(IAttitudeSensor attitudeSensor) {
		this.attitudeSensor = attitudeSensor;
	}

	@Override
	public void setAltitudeSensor(IAltitudeSensor altitudeSensor) {
		this.altitudeSensor = altitudeSensor;
	}

	@Override
	public void setETLSensor(IETL etlSensor) {
		this.etlSensor = etlSensor;
	}

	@Override
	public void setSpeedSensor(ISpeedSensor speedSensor) {
		this.speedSensor = speedSensor;
	}
	
	@Override
	public void setProximitySensor(IProximitySensor proximitySensor) {
		this.proximitySensor = proximitySensor;
	}
	
	@Override
	public IFlyingService performTheFlyingFunction() {
		// Implement the logic for the thermal fallback plan
		

		// Example logic: Use fadec, weatherSensor, and egtSensor to perform actions
		logger.info("Performing Glide to Safety Fallback Plan...");
		if(this.radioAltimeter.getGroundDistance() > 0) {
			handleGlideDescent();
		} else {
			stopTheFlyingFunction();
		}
		// Return the flying service or perform necessary actions
		return this; // Assuming this is the flying service being returned
	}
	
	@Override
	public IFlyingService stopTheFlyingFunction() {
		logger.info("Emergency Glide completed.");
		return this;
	}
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean ok = true;
		if(speedSensor == null) {
			logger.error("GlideToSafetyFallbackPlan: Speed Sensor service is not set.");
			ok = false;
		} else if (radioAltimeter == null) {
			logger.error("GlideToSafetyFallbackPlan: Radio Altimeter service is not set.");
			ok = false;
		} else if (controlSurfaces == null) {
			logger.error("GlideToSafetyFallbackPlan: Control Surfaces service is not set.");
			ok = false;
		} else if (attitudeSensor == null) {
			logger.error("GlideToSafetyFallbackPlan: Attitude Sensor service is not set.");
			ok = false;
		} else if (altitudeSensor == null) {
			logger.error("GlideToSafetyFallbackPlan: Altitude Sensor service is not set.");
			ok = false;
		} else if (etlSensor == null) {
			logger.error("GlideToSafetyFallbackPlan: ETL Sensor service is not set.");
			ok = false;
		}
			
			
		return ok;
	}
	
	private void handleGlideDescent() {
	    double altitude = altitudeSensor.getAltitude();
	    double groundAltitude = radioAltimeter.getRealGroundAltitude();
	    double realGroundDistance = altitude - groundAltitude;

	    double pitch = attitudeSensor.getPitch();
	    double speed = speedSensor.getSpeedGS();
	    double densityFactor = attitudeSensor.calculateDensityFactor(null);  // Pass null if weatherSensor not available
	    double correctionTime = PlaneSimulationElement.getTimeStep() * 2.0;

	    final double GLIDE_SPEED_TARGET = 120.0;  // Optimal glide speed (m/s)
	    final double FLARE_START = 30.0;          // Flare begins below 30 m AGL

	    // --- Descent Phase ---
	    if (realGroundDistance > FLARE_START) {
	        // Target glide pitch to maintain speed and descent angle
	        double desiredPitch = -3.5;  // Gentle descent angle
	        double pitchError = desiredPitch - pitch;
	        double pitchRate = pitchError / correctionTime;
	        double deflection = pitchRate / (0.6 * densityFactor);
	        deflection = clamp(deflection, 15.0, -15.0);
	        controlSurfaces.setElevatorDeflection(deflection);

	        // Airbrakes only if speed too high
	        double airbrake = (speed > GLIDE_SPEED_TARGET + 5.0) ? 0.2 : 0.0;
	        smoothAirbrakeAdjustment(airbrake);

	        logger.info(String.format(
	            "Gliding Descent: Alt=%.1f m, GroundDist=%.1f m, Speed=%.1f m/s, Pitch=%.1f°, Defl=%.1f°",
	            altitude, realGroundDistance, speed, pitch, deflection));

	    } else {
	        // --- Flare Phase ---
	        double desiredPitch = 3.0;  // Nose-up for touchdown
	        double pitchError = desiredPitch - pitch;
	        double pitchRate = pitchError / correctionTime;
	        double deflection = pitchRate / (0.6 * densityFactor);
	        deflection = clamp(deflection, 15.0, -15.0);
	        controlSurfaces.setElevatorDeflection(deflection);

	        // Deploy airbrakes for soft landing
	        smoothAirbrakeAdjustment(0.5);

	        logger.info(String.format(
	            "Glide Flare: GroundDist=%.1f m, Speed=%.1f m/s, Pitch=%.1f°, Defl=%.1f°",
	            realGroundDistance, speed, pitch, deflection));
	    }

	    // --- On Ground Handling ---
	    if (radioAltimeter.isOnGround()) {
	        controlSurfaces.setElevatorDeflection(0.0);
	        smoothAirbrakeAdjustment(1.0);  // Full brakes on ground
	        logger.info("Touchdown: Airbrakes Full, Elevator Neutral.");
	    }
	}

	private double clamp(double value, double max, double min) {
	    return Math.max(min, Math.min(max, value));
	}

	private void smoothAirbrakeAdjustment(double targetAirbrake) {
	    double currentAirbrake = controlSurfaces.getAirbrakeDeployment();
	    double maxChange = 0.1 * PlaneSimulationElement.getTimeStep();
	    double delta = clamp(targetAirbrake - currentAirbrake, maxChange, -maxChange);
	    controlSurfaces.setAirbrakeDeployment(clamp(currentAirbrake + delta, 1.0, 0.0));
	}


}
