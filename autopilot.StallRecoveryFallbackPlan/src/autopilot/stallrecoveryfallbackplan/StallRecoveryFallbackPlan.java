package autopilot.stallrecoveryfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IStallRecoveryFallbackPlan;
import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.autopilot.FallbackPlan;
import autonomousplane.infraestructure.devices.FADEC;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class StallRecoveryFallbackPlan extends FallbackPlan implements IStallRecoveryFallbackPlan {
	protected IFADEC fadec = null;
	protected ISpeedSensor speedSensor = null;
	protected IAttitudeSensor attitudeSensor = null;
	protected IAltitudeSensor altitudeSensor = null;
	protected IControlSurfaces controlSurface = null;
	protected IWeatherSensor weatherSensor = null; // Asumiendo que tienes un sensor de clima
	protected IAOASensor aoaSensor = null; // Asumiendo que tienes un sensor AOA
	public StallRecoveryFallbackPlan(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.addImplementedInterface(IStallRecoveryFallbackPlan.class.getName());
	}

	@Override
	public void setFADEC(IFADEC fadec) {
		this.fadec = fadec;
	}
	
	@Override
	public void setControlSurface(IControlSurfaces controlSurface) {
		this.controlSurface = controlSurface;
	}
	@Override
	public void setAttitudeSensor(IAttitudeSensor attitudeSensor) {
		this.attitudeSensor = attitudeSensor;
	}
	@Override
	public void setSpeedSensor(ISpeedSensor speedSensor) {
		this.speedSensor = speedSensor;
	}
	@Override
	public void setAltitudeSensor(IAltitudeSensor altitudeSensor) {
		this.altitudeSensor = altitudeSensor;
	}
	@Override
	public void setAOASensor(IAOASensor aoaSensor) {
		this.aoaSensor = aoaSensor;
	}
	@Override
	public void setWeatherSensor(IWeatherSensor weatherSensor) {
		this.weatherSensor = weatherSensor;
	}
	@Override
	public IFlyingService performTheFlyingFunction() {
	    if (!checkRequirementsToPerformTheFlyingService()) return null;

	    double timeStepSeconds = 0.05; // Ajusta según tu loop principal

	    // 1. Obtener valores actuales
	    double roll = attitudeSensor.getRoll();
	    double yaw = attitudeSensor.getYaw();
	    double pitch = attitudeSensor.getPitch();
	    double pitchRate = attitudeSensor.getPitchRate();

	    double tas = speedSensor.getSpeedTAS();
	    double verticalSpeed = altitudeSensor.getVerticalSpeed();

	    // 2. Calcular AOA
	    double aoa = this.aoaSensor.calculateAOA(pitch, verticalSpeed,  tas);
	    boolean inStall = aoa >= 15.0;

	    // 3. Si estamos en pérdida...
	 // Java
	    if (inStall && pitch >= 0) {
	        double targetAOA = 10.0; // Safe AOA below stall
	        double currentAOA = this.aoaSensor.calculateAOA(pitch, verticalSpeed, tas);
	        double deltaAOA = targetAOA - currentAOA;

	        double recoveryTime = 1.0; // seconds to recover
	        double requiredPitchRate = deltaAOA / recoveryTime;

	        double densityFactor = attitudeSensor.calculateDensityFactor(weatherSensor);
	        double elevatorDeflection = requiredPitchRate / (0.6 * densityFactor);

	        // Clamp elevator deflection to physical limits, e.g., [-15, 15] degrees
	        elevatorDeflection = Math.max(-15.0, Math.min(elevatorDeflection, 15.0));

	        controlSurface.setElevatorDeflection(elevatorDeflection);

	        fadec.setTHRUSTPercentage(100.0);
	    } else {
	    	controlSurface.setElevatorDeflection(0.0);
	    }


	    return this;
	}
	
	
	@Override
	public IFlyingService stopTheFlyingFunction() {
		logger.info("Stall recovery completed.");
		return this;
	}
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean ok = true;
		if (this.fadec == null) {
			logger.error("StallRecoveryFallbackPlan: FADEC service is not set.");
			ok = false;
		} else if (this.controlSurface == null) {
			logger.error("StallRecoveryFallbackPlan: Control Surface service is not set.");
			ok = false;
		} else if (this.attitudeSensor == null) {
			logger.error("StallRecoveryFallbackPlan: Attitude Sensor service is not set.");
			ok = false;
		} else if (this.speedSensor == null) {
			logger.error("StallRecoveryFallbackPlan: Speed Sensor service is not set.");
			ok = false;
		} else if (this.altitudeSensor == null) {
			logger.error("StallRecoveryFallbackPlan: Altitude Sensor service is not set.");
			ok = false;
		} else if (this.aoaSensor == null) {
			logger.error("StallRecoveryFallbackPlan: AOA Sensor service is not set.");
			ok = false;
		} else if (this.weatherSensor == null) {
			logger.error("StallRecoveryFallbackPlan: Weather Sensor service is not set.");
			ok = false;
		}
		
		return ok;
	}
	

}
