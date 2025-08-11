package autonomousplane.l1_basicnavigationassistance;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IL1_BasicNavigationAssistance;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.autopilot.L1_FlyingService;
import autonomousplane.interaction.interfaces.INotificationService;
import autonomousplane.simulation.simulator.PlaneSimulationElement;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L1_BasicNavigationAssistance extends L1_FlyingService implements IL1_BasicNavigationAssistance {

	public L1_BasicNavigationAssistance(BundleContext context, String id) {
		super(context, id);
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setStabilityModeActive(true);
	}

	@Override
	public IFlyingService performTheFlyingFunction() {
		logger.info("Performing L1 basic navigation assistance...");
        
		if (!checkServices()) {
			logger.error("Required services are not available. Cannot perform flying function.");
			return this;
		}
		double pitch = this.getAHRSSensor().getPitch();
        double roll = this.getAHRSSensor().getRoll();
        double yaw = this.getAHRSSensor().getYaw();
		//AOA Sensor is used to monitor the flying parameters
		boolean correction_required = false;
		
		// AHRS Sensor is used to monitor the flying parameters
		/*this.getAHRSSensor().setPitch(pitch + AHRSSensor.getPitchRate());
		this.getAHRSSensor().setRoll(roll + AHRSSensor.getRollRate());
		this.getAHRSSensor().setYaw(yaw + AHRSSensor.getYawRate());
		 */
        // New, wider safe ranges
		if(this.getStabilityModeActive() ) {
	        double SAFE_PITCH_MIN = 2;
	        double SAFE_PITCH_MAX = 5;
	        double SAFE_ROLL_MIN = -3;
	        double SAFE_ROLL_MAX = 3;
	        double MIN_ROLL_ERROR_THRESHOLD = 0.1; // degrees
	        double correctionTime = PlaneSimulationElement.getTimeStep() * 2;

	        if (roll >= SAFE_ROLL_MIN && roll <= SAFE_ROLL_MAX) {
	            this.getControlSurfaces().setAileronDeflection(0.0);
	            logger.info("Roll within safe range, aileron neutral.");
	        } else {	            double desiredRoll;
	            if (roll > SAFE_ROLL_MAX) {
	                // Too much positive roll, correct negatively
	                desiredRoll = clamp(roll - 1.5, SAFE_ROLL_MAX, SAFE_ROLL_MIN);
	            } else {
	                // Too much negative roll, correct positively
	                desiredRoll = clamp(roll + 1.5, SAFE_ROLL_MAX, SAFE_ROLL_MIN);
	            }
	            double rollError = desiredRoll - roll;
	            double desiredRollRate = rollError / correctionTime;
	            double requiredDeflection = desiredRollRate / (1.0);
	            requiredDeflection = clamp(requiredDeflection, 15.0, -15.0);

	            if (Math.abs(rollError) < MIN_ROLL_ERROR_THRESHOLD) {
	                this.getControlSurfaces().setAileronDeflection(0.0);
	            } else {
	                this.getControlSurfaces().setAileronDeflection(requiredDeflection);
	                logger.info(String.format(
	                    "Roll correction: roll=%.2f°, desired=%.2f°, deflection=%.2f°",
	                    roll, desiredRoll, requiredDeflection
	                ));
	            }
	        }

            boolean unstablePitch = pitch < SAFE_PITCH_MIN || pitch > SAFE_PITCH_MAX;
            
            if (unstablePitch) {
                correction_required = true;
                double desiredPitch;
                if (pitch > SAFE_PITCH_MAX) {
                    // Too much positive pitch, correct negatively
                    desiredPitch = clamp(pitch - 1.5, SAFE_PITCH_MAX, SAFE_PITCH_MIN);
                } else {
                    // Too much negative pitch, correct positively
                    desiredPitch = clamp(pitch + 1.5, SAFE_PITCH_MAX, SAFE_PITCH_MIN);
                }
                double pitchError = desiredPitch - pitch;
                double desiredPitchRate = pitchError / correctionTime;
                double requiredDeflection = desiredPitchRate / (0.6 );

                if (Math.abs(pitchError) < MIN_ROLL_ERROR_THRESHOLD) {
                    this.getControlSurfaces().setElevatorDeflection(0.0);
                } else {
                    logger.info(String.format("Flight: pitch correction -> current=%.2f°, error=%.2f°, deflection=%.2f°",
                        pitch, pitchError, requiredDeflection));
                    logger.info("Correcting Pitch angle...");
                    this.getControlSurfaces().setElevatorDeflection(requiredDeflection);
                }
            } else {
                this.getControlSurfaces().setElevatorDeflection(0.0);
            }
	        
		}
	
        
		if ( !correction_required ) {
			logger.info("Monitoring flying parameters. Nothing to warn ...");
		}
		
		return this;

	}
	
	private double clamp(double rate, double maxRate, double min_rate) {
	    return Math.max(min_rate, Math.min(rate, maxRate));
	}
	private boolean checkServices() {
	    IAttitudeSensor attitudeSensor = OSGiUtils.getService(context, IAttitudeSensor.class);
	    IControlSurfaces controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
	    INotificationService notificationService = OSGiUtils.getService(context, INotificationService.class);

	    return attitudeSensor != null &&
	           controlSurfaces != null &&
	           notificationService != null;
	}
}
