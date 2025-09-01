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
import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.autopilot.FallbackPlan;
import autonomousplane.infraestructure.autopilot.FlyingService;
import autonomousplane.infraestructure.autopilot.L2_FlyingService;
import autonomousplane.infraestructure.devices.FADEC;
import autonomousplane.simulation.simulator.PlaneSimulationElement;
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

	    double timeStepSeconds = PlaneSimulationElement.getTimeStep(); // Paso de simulación (p. ej., 0.1s)

	    // 1. Obtener valores actuales
	    double roll = attitudeSensor.getRoll();
	    double yaw = attitudeSensor.getYaw();
	    double pitch = attitudeSensor.getPitch();

	    double tas = speedSensor.getSpeedTAS();           // Velocidad aire (vx)
	    double verticalSpeed = altitudeSensor.getVerticalSpeed(); // Velocidad vertical (vz)

	    // 2. Calcular AOA
	    double currentAOA = this.aoaSensor.calculateAOA(tas, verticalSpeed, pitch);
	    boolean inStall = currentAOA >= 12.0;

	    // 3. Si estamos en pérdida...
	    if (inStall && pitch >= 0) {
	    	logger.info("Stall detected! Current AOA: " + currentAOA + " degrees");
	        double targetAOA = 10.0; // Objetivo seguro por debajo del stall
	        double deltaPitchRequired = targetAOA - currentAOA; // Aproximamos que deltaPitch ≈ deltaAOA

	        double recoveryTime = timeStepSeconds *2 ; // Tiempo razonable para recuperación (segundos)
	        double requiredPitchRate = deltaPitchRequired / recoveryTime;

	        // Límite opcional para evitar pitchRate brusco
	        requiredPitchRate = Math.max(-5.0, Math.min(requiredPitchRate, 5.0));

	        double densityFactor = attitudeSensor.calculateDensityFactor(weatherSensor);
	        double elevatorDeflection = requiredPitchRate / (0.6 * densityFactor);

	        // Limitar deflexión física realista del elevador
	        elevatorDeflection = Math.max(-15.0, Math.min(elevatorDeflection, 15.0));

	       
	        

	        // Aplicar recuperación
	        controlSurface.setElevatorDeflection(elevatorDeflection);
	        fadec.setTHRUSTPercentage(95.0); // Máximo empuje durante la recuperación
	    } else {
	    	System.out.println("No stall detected. Current AOA: " + currentAOA + " degrees.");
	        controlSurface.setElevatorDeflection(0.0); // Sin pérdida → elevador neutral
	        IFlyingService flyingService = OSGiUtils.getService(
	        	    context,
	        	    IFlyingService.class,
	        	    "(&" +
	        	      "(!(id=autopilot.StallRecoveryFallbackPlan))" +
	        	      "(!(id=autopilot.ThermalFallbackPlan))" +
	        	    ")"
	        	);

	     // After stall recovery
	        if (flyingService != null) {
		        System.out.println(flyingService.getClass().getName() + " is the normal flying service.");

	            flyingService.startFlight();
	            this.stopTheFlyingFunction();
	            System.out.println("Stall recovery completed, resuming normal flight.");
	            
	            return flyingService; // <-- Return the normal flying service
	        } else {
	            logger.error("StallRecoveryFallbackPlan: No IFlyingService available to continue normal flight.");
	            return this;
	        }

	        
	    }

	    return this;
	}

	
	
	@Override
	public IFlyingService stopTheFlyingFunction() {
		logger.info("Stall recovery completed.");
		this.setProperty(FlyingService.ACTIVE, false);
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
