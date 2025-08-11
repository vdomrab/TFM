package autopilot.emergencylandingfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IEmergencyLandingFallbackPlan;
import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IETL;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.autopilot.FallbackPlan;
import autonomousplane.simulation.simulator.PlaneSimulationElement;

public class EmergencyLandingFallbackPlan extends FallbackPlan implements IEmergencyLandingFallbackPlan {

	private IRadioAltimeterSensor radioAltimeter = null;
	private IControlSurfaces controlSurfaces = null;
	private IAttitudeSensor attitudeSensor = null;
	private IAltitudeSensor altitudeSensor = null;
	private IETL etlSensor = null;
	private ISpeedSensor speedSensor = null;
	private IProximitySensor proximitySensor = null;
	private IFADEC fadec = null;
	private ILandingSystem landingSystem = null;
	private IFuelSensor fuelSensor = null;
	private IWeatherSensor weatherSensor = null;
	public EmergencyLandingFallbackPlan(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		this.addImplementedInterface(IEmergencyLandingFallbackPlan.class.getName());
	}
	
	public void setRadioAltimeter(IRadioAltimeterSensor radioAltimeter) {
		this.radioAltimeter = radioAltimeter;
	}
	@Override
	public void setControlSurfaces(IControlSurfaces controlSurfaces) {
		this.controlSurfaces = controlSurfaces;
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
	public void setFADEC(IFADEC fadec) {
		this.fadec = fadec;
	}
	@Override
	public void setFuelSensor(IFuelSensor fuelSensor) {
		this.fuelSensor = fuelSensor;
	}
	@Override
	public void setLandingSystem(ILandingSystem landingSystem) {
		this.landingSystem = landingSystem;
	}
	public void setWeatherSensor(IWeatherSensor weatherSensor) {
		this.weatherSensor = weatherSensor;
	}
	@Override
	public IFlyingService performTheFlyingFunction() {
		// Implement the logic for the thermal fallback plan
		
		if(checkRequirementsToPerformTheFlyingService() == false) {
			logger.error("Required components are not set for Emergency Landing Fallback Plan.");
			return null; // or throw an exception
		}
	    double altitude = altitudeSensor.getAltitude();
	    double groundAltitude = radioAltimeter.getRealGroundAltitude();
	    double realGroundDistance = altitude - groundAltitude;
		if(realGroundDistance > 0) {
			// Handle emergency descent
			handleImmediateEmergencyDescent();
			logger.info("Performing Emergency Landing fallback actions.");

		} else {
			// Handle landing procedures
			logger.info("Emergency Landing Finished.");
			// Perform landing logic here
			this.stopTheFlyingFunction();
			// Additional landing logic can be added here
		}
		// Example logic: Use fadec, weatherSensor, and egtSensor to perform actions
		
		// Return the flying service or perform necessary actions
		return this; // Assuming this is the flying service being returned
	}
	
	@Override
	public IFlyingService stopTheFlyingFunction() {
		logger.info("Emergency Landing Completed.");
		return this;
	}
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean ok = true;
		if(speedSensor == null) {
			logger.error("EmergencyLandingFallbackPlan: Speed Sensor service is not set.");
			ok = false;
		} else if (radioAltimeter == null) {
			logger.error("EmergencyLandingFallbackPlan: Radio Altimeter service is not set.");
			ok = false;
		} else if (controlSurfaces == null) {
			logger.error("EmergencyLandingFallbackPlan: Control Surfaces service is not set.");
			ok = false;
		} else if (attitudeSensor == null) {
			logger.error("EmergencyLandingFallbackPlan: Attitude Sensor service is not set.");
			ok = false;
		} else if (altitudeSensor == null) {
			logger.error("EmergencyLandingFallbackPlan: Altitude Sensor service is not set.");
			ok = false;
		} else if (etlSensor == null) {
			logger.error("EmergencyLandingFallbackPlan: ETL Sensor service is not set.");
			ok = false;
		}else if (fadec == null) {
			logger.error("EmergencyLandingFallbackPlan: FADEC service is not set.");
			ok = false;
		} else if (fuelSensor == null) {
			logger.error("EmergencyLandingFallbackPlan: Fuel Sensor service is not set.");
			ok = false;
		} else if (landingSystem == null) {
			logger.error("EmergencyLandingFallbackPlan: Landing System service is not set.");
			ok = false;
		} else if (weatherSensor == null) {
			logger.error("EmergencyLandingFallbackPlan: Weather Sensor service is not set.");
			ok = false;
		}
			
			
		return ok;
	}
	
	private void handleImmediateEmergencyDescent() {
	    double altitude = altitudeSensor.getAltitude();
	    double groundAltitude = radioAltimeter.getRealGroundAltitude();
	    double realGroundDistance = altitude - groundAltitude;

	    double pitch = attitudeSensor.getPitch();
	    double thrust = fadec.getCurrentThrust();
	    double speed = speedSensor.getSpeedGS();
	    double densityFactor = attitudeSensor.calculateDensityFactor(weatherSensor);
	    double correctionTime = PlaneSimulationElement.getTimeStep() * 2.0;

	    final double LANDING_SPEED = 75.0;         // m/s (~270 km/h)
	    final double DESCENT_SPEED = 140.0;        // m/s (~500 km/h)
	    final double FLARE_START = 30.0;           // Start flare at 30 m above ground

	    // --- Descent or Flare ---
	    if (realGroundDistance > FLARE_START) {
	        // Normal Descent Phase (start immediately)
	        double desiredPitch = -4.0;  // Smooth descent
	        double pitchError = desiredPitch - pitch;
	        double pitchRate = pitchError / correctionTime;
	        double deflection = pitchRateToDeflection(pitchRate, densityFactor);
	        controlSurfaces.setElevatorDeflection(deflection);

	        // Control thrust to maintain descent speed
	        double speedError = DESCENT_SPEED - speed;
	        double targetThrust = clamp(thrust + speedError * 0.5, 20.0, 50.0);
	        fadec.setTHRUSTPercentage(targetThrust);

	        // Apply airbrakes only if too fast
	        double airbrake = (speed > DESCENT_SPEED + 5.0) ? 0.2 : 0.0;
	        smoothAirbrakeAdjustment(airbrake);

	        logger.info(String.format(
	            "Emergency Descent: Alt=%.1f m, GroundDist=%.1f m, Speed=%.1f m/s, Pitch=%.1f°, Defl=%.1f°, Thrust=%.1f%%",
	            altitude, realGroundDistance, speed, pitch, deflection, targetThrust));

	    } else {
	        // Flare Phase (soft touchdown)
	        double desiredPitch = 2.0;  // Nose-up for landing
	        double pitchError = desiredPitch - pitch;
	        double pitchRate = pitchError / correctionTime;
	        double deflection = pitchRateToDeflection(pitchRate, densityFactor);
	        controlSurfaces.setElevatorDeflection(deflection);

	        // Thrust down to idle
	        double thrustDelta = clamp(0.0 - thrust, 2.0, -2.0);
	        fadec.setTHRUSTPercentage(thrust + thrustDelta);

	        // Airbrake for landing
	        smoothAirbrakeAdjustment(0.5);

	        logger.info(String.format(
	            "Landing Flare: GroundDist=%.1f m, Speed=%.1f m/s, Pitch=%.1f°, Defl=%.1f°, Thrust=%.1f%%",
	            realGroundDistance, speed, pitch, deflection, thrust));
	    }

	    // On-Ground Handling
	    if (radioAltimeter.isOnGround()) {
	        controlSurfaces.setElevatorDeflection(0.0);
	        fadec.setTHRUSTPercentage(0.0);
	        smoothAirbrakeAdjustment(1.0); // full airbrakes
	        logger.info("Touchdown: Thrust 0%, Airbrakes Full, Elevator Neutral.");
	    }
	}
	private double pitchRateToDeflection(double pitchRate, double densityFactor) {
	    double deflection = pitchRate / (0.6 * densityFactor);
	    return clamp(deflection, 15.0, -15.0);
	}

	private void smoothAirbrakeAdjustment(double targetAirbrake) {
	    double currentAirbrake = controlSurfaces.getAirbrakeDeployment();
	    double maxChange = 0.1 * PlaneSimulationElement.getTimeStep();
	    double delta = clamp(targetAirbrake - currentAirbrake, maxChange, -maxChange);
	    controlSurfaces.setAirbrakeDeployment(clamp(currentAirbrake + delta, 1.0, 0.0));
	}

	private double clamp(double value, double max, double min) {
	    return Math.max(min, Math.min(max, value));
	}
}
