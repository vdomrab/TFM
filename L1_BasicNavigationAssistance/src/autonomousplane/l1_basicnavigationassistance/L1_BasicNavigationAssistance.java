package autonomousplane.l1_basicnavigationassistance;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IL1_BasicNavigationAssistance;
import autonomousplane.infraestructure.autopilot.L1_FlyingService;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L1_BasicNavigationAssistance extends L1_FlyingService implements IL1_BasicNavigationAssistance {

	public L1_BasicNavigationAssistance(BundleContext context, String id) {
		super(context, id);
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setStabilityModeActive(true);
	}

	@Override
	public IFlyingService performTheFlyingFunction() {
		System.out.println("Performing basic navigation assistance L1...");
		logger.info("Performing basic navigation assistance...");
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
        double SAFE_PITCH_MIN = -5.0;
        double SAFE_PITCH_MAX = 10.0;
        double SAFE_ROLL_MIN = -8.0;
        double SAFE_ROLL_MAX = 8.0;
        
        boolean unstablePitch = pitch < SAFE_PITCH_MIN || pitch > SAFE_PITCH_MAX;
        boolean unstableRoll = roll < SAFE_ROLL_MIN || roll > SAFE_ROLL_MAX;
        double correctionTime = 3.0; // segundos
        Boolean stabilityMode = (Boolean) this.getProperty("stability-mode-active");
        boolean stabilityModeActive = stabilityMode != null ? stabilityMode.booleanValue() : false;
        System.out.println(unstableRoll);
	    if(stabilityModeActive) { // --- Roll correction --- //Cambiarlo
		     if (unstableRoll) {
		    	 correction_required = true;
		         double desiredRoll = clamp(roll, SAFE_ROLL_MIN, SAFE_ROLL_MAX);
		         double rollError = desiredRoll - roll;
		
		         double desiredRollRate = rollError / correctionTime;
		         double requiredDeflection = desiredRollRate / 2.0; // factor rollRate/deflection
		         logger.info(String.format("Roll correction: current=%.2f°, error=%.2f°, deflection=%.2f°", roll, rollError, requiredDeflection));
		         logger.info("Correcting roll angle...");
		         this.getControlSurfaces().setAileronDeflection(requiredDeflection);
		     }
		     // --- Pitch correction ---
		     if (unstablePitch) {
		    	 correction_required = true;

		         double desiredPitch = clamp(pitch, SAFE_PITCH_MIN, SAFE_PITCH_MAX);
		         double pitchError = desiredPitch - pitch;
		
		         double desiredPitchRate = pitchError / correctionTime;
		         double requiredDeflection = -desiredPitchRate / 1.5; // factor pitchRate/deflection
		         logger.info(String.format("Pitch correction: current=%.2f°, error=%.2f°, deflection=%.2f°",
		        		    pitch, pitchError, requiredDeflection));
		         logger.info("Correcting pitch angle...");
		         this.getControlSurfaces().setElevatorDeflection(requiredDeflection);
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

}
