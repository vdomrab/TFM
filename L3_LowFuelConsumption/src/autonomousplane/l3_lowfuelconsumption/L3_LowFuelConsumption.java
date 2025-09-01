package autonomousplane.l3_lowfuelconsumption;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IEmergencyLandingFallbackPlan;
import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IGlideToSafetyFallbackPlan;
import autonomousplane.autopilot.interfaces.IL3_AdvancedAutomation;
import autonomousplane.autopilot.interfaces.IStallRecoveryFallbackPlan;
import autonomousplane.autopilot.interfaces.IThermalFallbackPlan;
import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.autopilot.L3_FlyingService;
import autonomousplane.infraestructure.devices.EGTSensor;
import autonomousplane.infraestructure.devices.SpeedSensor;
import autonomousplane.interaction.interfaces.INotificationService;
import autonomousplane.interfaces.EFlyingStages;
import autonomousplane.simulation.simulator.PlaneSimulationElement;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
public class L3_LowFuelConsumption extends L3_FlyingService implements IL3_AdvancedAutomation {
	public L3_LowFuelConsumption(BundleContext context, String id) {
		super(context, id);
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setStabilityModeActive(true);
		this.setLowFuelMode();
	}
	
	protected void setLowFuelMode() {
		if(this.getFuelSensor() != null) {
			if(!this.getFuelSensor().isLowFuelMode()) {
				this.getFuelSensor().setLowFuelMode(true);
			}
		}
	}
	@Override
	public IFlyingService performTheFlyingFunction() {
		if(checkServices()) {
					    logger.info("Performing flying function in L3_LowFuelConsumption.");

		    boolean correctionRequired = false;
		    this.setLowFuelMode(); // Ensure low fuel mode is set
		    double pitch = this.AHRSSensor.getPitch();
		    double roll = this.AHRSSensor.getRoll();
	        EFlyingStages stage = this.navigationSystem.getCurrentFlyghtStage();

	        correctionRequired |= handleEngineFailure();
	        correctionRequired |= checkTerrainAwareness(stage, radioAltimeterSensor.getGroundDistance(), altimeterSensor.getVerticalSpeed());
	        correctionRequired |= handleStallWarnings();
	        correctionRequired |= hangleEngineHeating();
	        correctionRequired |= handleLowFuel();
	        correctionRequired |= handleObjectsProximity(this.getProximitySensor().isObjectDetected());
	        if (this.getStabilityModeActive()) {
		        correctionRequired |= correctRollIfNeeded(roll);

		        switch (stage) {
		            case CLIMB:
		                correctionRequired |= handleClimbPhase(pitch);
		                break;
		            case DESCENT:
		                manageDescentAndApproach();
		                break;
		            case CRUISE:
		                adjustPitchThrustToMaintainAltitudeAndSpeedCruise();
		                break;
		            case TAKEOFF:
		               correctionRequired |= handleTakeoffPhase(pitch);
		                break;
		            case LANDING:
		                manageDescentAndApproach();
		                break;
		            default:
		                break; // Other phases ignored here
		        }
		    }
		       
		        if (!correctionRequired) {
		            logger.info("Monitoring flying parameters. Nothing to warn ...");
		        }
		    
		    } else {
		        logger.error("Cannot perform flying function missing essential components.");
		    }
		    return this;
		}
	public boolean hangleEngineHeating() {
	    boolean correctionRequired = false;

		if (this.getEGTSensor().getTemperature() > EGTSensor.OVERHEAT_THRESHOLD_C) {
	    	 if(notificationService != null && notificationService.isMechanismAvailable("OverheatWarning")) {
	             this.getNotificationService().notify("⚠️ Engine overheat detected! Cooling system activated.", "OverheatWarning");
	             if(this.getFallbackPlan() != null && this.getFallbackPlan() instanceof IThermalFallbackPlan) {
	            	 this.activateTheFallbackPlan(); // Activa el plan de contingencia
	             }
	             correctionRequired = true; // Indica que se ha activado el sistema de refrigeración

	    	 }
	     } else if (this.getWeatherSensor().getTemperature() < EGTSensor.ICE_DANGER_OAT_THRESHOLD_C && this.getWeatherSensor().getHumidity() > 70.0) {
	    	 if(notificationService != null && notificationService.isMechanismAvailable("FrozenWarning")) {
	             this.getNotificationService().notify("⚠️ Engine Freeeze! Heating system activated.", "FrozenWarning");
	             if(this.getFallbackPlan() != null && this.getFallbackPlan() instanceof IThermalFallbackPlan) {
	            	 this.activateTheFallbackPlan(); // Activa el plan de contingencia
	             }
	             correctionRequired = true; // Indica que se ha activado el sistema de refrigeración
	             
	    	 }
	     }
	   return correctionRequired;
	    	 
	}
	public boolean handleLowFuel() {
	    boolean correctionRequired = false;
	    double fuelLevel = fuelSensor.getFuelLevel();
	    double estimatedRange = fuelSensor.getEstimatedRangeMeters(this.getSpeedSensor().getSpeedTAS()); // True Airspeed in m/s
	    double distanceToAirport = this.getNavigationSystem().getTotalDistance()- this.getNavigationSystem().getCurrentDistance();// meters
	    
	    if (fuelLevel < 3000.0 && estimatedRange < 1.5 * distanceToAirport) {
	    	 if(notificationService != null && notificationService.isMechanismAvailable("LowFuelWarning")) {
		    	     this.getNotificationService().notify("⚠️ Critical fuel warning! Estimated range: " + estimatedRange + " meters.", "LowFuelWarning");
	    	 }
	    	 if(this.getFallbackPlan() != null && this.getFallbackPlan() instanceof IEmergencyLandingFallbackPlan) {
	             correctionRequired = true; // Indica que se ha activado el plan de contingencia
	             logger.info("Activating fallback plan due to critical fuel level.");
	            
	             this.activateTheFallbackPlan(); // Activate fallback plan if fuel is critically low
	    	 }
	    } else if (fuelLevel < 6000.0 && estimatedRange < 2.0 * distanceToAirport) {
	    	 if(notificationService != null && notificationService.isMechanismAvailable("LowFuelWarning")) {
	    	     this.getNotificationService().notify("⚠️ Low fuel warning! Estimated range: " + estimatedRange + " meters.", "LowFuelWarning");
	    	 }
	    }
	    return correctionRequired;
	}
	public boolean handleEngineFailure() {
	    boolean correctionRequired = false;    
		if (this.getFADEC().isFailure()) {     
				logger.error("FADEC service is present but unregistered.");
				if(this.getFallbackPlan() != null && this.getFallbackPlan() instanceof IGlideToSafetyFallbackPlan) {
	            	 this.activateTheFallbackPlan(); // Activa el plan de contingencia
	             }
                correctionRequired = true;
            }
 
	 
	    return correctionRequired;
	}
	
	private boolean correctRollIfNeeded(double roll) {
	    final double SAFE_ROLL_MIN = -8.0;
	    final double SAFE_ROLL_MAX = 8.0;
	    final double MIN_ROLL_ERROR_THRESHOLD = 0.1;
	    double correctionTime = PlaneSimulationElement.getTimeStep() * 2;

	    if (roll >= SAFE_ROLL_MIN && roll <= SAFE_ROLL_MAX) {
	        this.getControlSurfaces().setAileronDeflection(0.0);
	        logger.info("Roll within safe range, aileron neutral.");
	        return false;
	    }

	    double densityFactor = this.AHRSSensor.calculateDensityFactor(this.weatherSensor);
	    double desiredRoll = roll > SAFE_ROLL_MAX ? clamp(roll - 1.5, SAFE_ROLL_MAX, SAFE_ROLL_MIN)
	                                              : clamp(roll + 1.5, SAFE_ROLL_MAX, SAFE_ROLL_MIN);
	    double rollError = desiredRoll - roll;
	    double desiredRollRate = rollError / correctionTime;
	    double requiredDeflection = clamp(desiredRollRate / (1.0 * densityFactor), 15.0, -15.0);

	    if (Math.abs(rollError) < MIN_ROLL_ERROR_THRESHOLD) {
	        this.getControlSurfaces().setAileronDeflection(0.0);
	    } else {
	        this.getControlSurfaces().setAileronDeflection(requiredDeflection);
	        logger.info(String.format("Roll correction: roll=%.2f°, desired=%.2f°, deflection=%.2f°",
	                                  roll, desiredRoll, requiredDeflection));
	    }

	    return true;
	}

	private boolean handleClimbPhase(double pitch) {
	    final double SAFE_PITCH_MIN = 7.0;
	    final double SAFE_PITCH_MAX = 13.0;
	    boolean correctionDone = correctPitchIfNeeded(pitch, SAFE_PITCH_MIN, SAFE_PITCH_MAX, "CLIMB");
	    double realaltitude = altimeterSensor.getAltitude() - radioAltimeterSensor.getRealGroundAltitude();
		if(realaltitude > 2000.0 && ( this.getFADEC().getCurrentThrust() <= 60 || this.getFADEC().getCurrentThrust() > 89.0) ) {
	        this.getFADEC().setTHRUSTPercentage(75.0);
	        logger.info("CLIMB: Changing thrust to 75%");
	        correctionDone = true;

    	}else if (realaltitude <= 2000){
	    
    		if (this.getFADEC().getCurrentThrust() < 91.0) {
	    
		        this.getFADEC().setTHRUSTPercentage(91.0);
		        logger.info("CLIMB: Changing thrust to 91%");	
	    	}
	        correctionDone = true;
	    }

	    if (this.getControlSurfaces().getAirbrakeDeployment() > 0.0) {
	        this.getControlSurfaces().setAirbrakeDeployment(0.0);
	        logger.info("CLIMB: retracting airbrakes.");
	        correctionDone = true;
	    }

	    return correctionDone;
	}

	public void manageDescentAndApproach() {
	    double altitude = altimeterSensor.getAltitude() - radioAltimeterSensor.getRealGroundAltitude();
	    double pitch = this.getAHRSSensor().getPitch();
	    double speed = this.getSpeedSensor().getSpeedGS();
	    double correctionTime = PlaneSimulationElement.getTimeStep() * 2;
	    double densityFactor = this.getAHRSSensor().calculateDensityFactor(weatherSensor);

	    double totalDistance = this.getNavigationSystem().getTotalDistance();
	    double currentDistance = this.getNavigationSystem().getCurrentDistance();
	    double distanceToTarget = totalDistance - currentDistance;

	    // Objetivos de altitud y velocidad según fase de aproximación
	    //double targetAltitude = 2850.0;
	    double targetSpeed = 90;

	    if (distanceToTarget <= 35500.0) {
	      //  targetAltitude = 300.0;
	      //  targetSpeed = 60.0;
	    //    // =================== YAW ALIGNMENT WITH RUNWAY ===================
	        double currentYaw = this.AHRSSensor.getYaw();
	        double runwayHeading = this.getLandingSystem().getRunwayHeadingDegrees();
	        double yawError = runwayHeading - currentYaw;

	        // Normaliza yawError a [-180°, 180°] para el giro más eficiente
	        if (yawError > 180.0) yawError -= 360.0;
	        if (yawError < -180.0) yawError += 360.0;

	        double correctionTimeYaw = correctionTime * 1.2; // Tiempo de corrección para yaw
	        double desiredYawRate = yawError / correctionTimeYaw;
	        double yawDensityFactor = this.AHRSSensor.calculateDensityFactor(this.weatherSensor);

	        double requiredRudderDeflection = desiredYawRate / (0.4 * yawDensityFactor);
	        requiredRudderDeflection = clamp(requiredRudderDeflection, 20.0, -20.0);
	        if (Math.abs(yawError) > 2.0) { // or a small value
	            this.getControlSurfaces().setRudderDeflection(requiredRudderDeflection);
	            logger.info(String.format(
	                "LANDING: Aligning with runway heading (yaw error=%.2f°, rudder=%.2f°)",
	                yawError, requiredRudderDeflection
	            ));
	        } else {
	            this.getControlSurfaces().setRudderDeflection(0.0);
	            logger.info("LANDING: Aligned with runway, rudder neutral.");
	        }
	    }

	    // --- Control de Altitud ---
	 
	        // Está demasiado alto → descender
	        double desiredPitch = -7; // forzar pitch negativo
	        double pitchError = desiredPitch - pitch;
	        double desiredPitchRate = pitchError / correctionTime;

	        double requiredDeflection = Math.abs(pitchError) < 0.1
	            ? 0.0
	            : desiredPitchRate / (0.6 * densityFactor);


	        requiredDeflection = clamp(requiredDeflection, 15.0, -15.0);
	        this.getControlSurfaces().setElevatorDeflection(requiredDeflection);

	       

	    // --- Control de Velocidad (suavizado) ---
	    final double MAX_THRUST = 110.0;
	    final double MIN_THRUST = 20.0;

	    double currentThrust = this.getFADEC().getCurrentThrust();
	    double pitchRad = Math.toRadians(pitch);
	    double thrustEfficiency = 1.0 - Math.min(altitude / 15000.0, 0.3);
	    double airDensity = this.getWeatherSensor().getAirDensity();
	    double airBrakeLevel = this.getControlSurfaces().getAirbrakeDeployment();
	    double effectiveCd = SpeedSensor.DRAG_COEFFICIENT + (SpeedSensor.AIR_BRAKE_DRAG_COEFFICIENT * airBrakeLevel);
	    double dragForce = 0.5 * effectiveCd * airDensity * SpeedSensor.FRONTAL_AREA * speed * speed;

	    double requiredThrust = (dragForce / Math.max(0.01, Math.cos(pitchRad))) / Math.max(0.01, thrustEfficiency);
	    double thrustPercent = (requiredThrust / SpeedSensor.MAX_THRUST_FORCE) * 100.0;
	    thrustPercent = clamp(thrustPercent, MAX_THRUST, MIN_THRUST);

	    // Ajuste por error de velocidad
	    double speedError = targetSpeed - speed;
	    double thrustCorrection = thrustPercent + 1.2 * speedError;
	    thrustCorrection = clamp(thrustCorrection, MAX_THRUST, MIN_THRUST);

	    // Suavizado de cambios de empuje
	    this.getFADEC().setTHRUSTPercentage(thrustCorrection);


	    // --- Control de aerofrenos ---
	    double targetAirbrake = 0.0;
	    if (speed > targetSpeed + 3.0) {
	        targetAirbrake = Math.min(0.3, 0.05 * (speed - targetSpeed));
	    }

	    double currentAirbrake = this.getControlSurfaces().getAirbrakeDeployment();
	    double maxAirbrakeChangePerSec = 0.1;
	    double airbrakeDelta = clamp(targetAirbrake - currentAirbrake, maxAirbrakeChangePerSec * PlaneSimulationElement.getTimeStep(), -maxAirbrakeChangePerSec * PlaneSimulationElement.getTimeStep());
	    double smoothedAirbrake = clamp(currentAirbrake + airbrakeDelta, 1.0, 0.0);
	    this.getControlSurfaces().setAirbrakeDeployment(smoothedAirbrake);

	    // Logging final
	   
	}



	private boolean correctPitchIfNeeded(double pitch, double safeMin, double safeMax, String phase) {
	    final double MIN_ERROR_THRESHOLD = 0.1;
	    double correctionTime = PlaneSimulationElement.getTimeStep() * 2;

	    if (pitch >= safeMin && pitch <= safeMax) {
	        this.getControlSurfaces().setElevatorDeflection(0.0);
	        return false;
	    }

	    double densityFactor = this.AHRSSensor.calculateDensityFactor(this.weatherSensor);
	    double desiredPitch = pitch > safeMax ? clamp(pitch - 1.5, safeMax, safeMin)
	                                          : clamp(pitch + 1.5, safeMax, safeMin);
	    double pitchError = desiredPitch - pitch;
	    double desiredPitchRate = pitchError / correctionTime;
	    double requiredDeflection = desiredPitchRate / (0.6 * densityFactor);

	    if (Math.abs(pitchError) < MIN_ERROR_THRESHOLD) {
	        this.getControlSurfaces().setElevatorDeflection(0.0);
	    } else {
	        logger.info(String.format("%s: pitch correction -> current=%.2f°, error=%.2f°, deflection=%.2f°, densityFactor=%.3f",
	                                  phase, pitch, pitchError, requiredDeflection, densityFactor));
	        this.getControlSurfaces().setElevatorDeflection(requiredDeflection);
	    }

	    return true;
	}

	public boolean handleStallWarnings() {
	    double aoa = this.getAOASensor().getAOA();
	    if (aoa > 12 && aoa < 15) {
	        notifyStallCondition("approaching stall condition", aoa);
	        return true;
	    } else if (aoa >= 15) {
	        notifyStallCondition("is in stall condition", aoa);
        	if(this.getFallbackPlan() != null && this.getFallbackPlan() instanceof IStallRecoveryFallbackPlan) {
            	 this.activateTheFallbackPlan(); // Activa el plan de contingencia
	             
	        } else {
	            logger.error("No fallback plan available to handle stall condition.");
	        }
	        return true;
	    }
	    return false;
	}


	private void notifyStallCondition(String message, double aoa) {
	    if (this.getNotificationService() != null && this.getNotificationService().isMechanismAvailable("StallWarning")) {
	        this.getNotificationService().notify("Plane " + message + ": " + aoa, "StallWarning");
	    }
	}

	private double clamp(double value, double max, double min) {
	    return Math.max(min, Math.min(value, max));
	}

	
	public boolean checkTerrainAwareness(
			EFlyingStages flyingStage,
		    double agl,                  // Altura sobre el terreno (radio altímetro)
		    double verticalSpeed        // Velocidad vertical (m/s       // Ground speed (opcional, pero útil)
		) {
		    // Valores por defecto
		    double minAGLThreshold = 900.0;         // metros
		    double maxDescentRate = -3.0;           // m/s
		    double timeToImpactThreshold = 15.0;    // segundos

		    // Ajustes según fase de vuelo
		    switch (this.navigationSystem.getCurrentFlyghtStage()) {
		    case EFlyingStages.CLIMB:
		        minAGLThreshold = 1000.0;    // ≈ 3300 ft, margen alto para evitar CFIT en salida
		        maxDescentRate = 0.0;        // no debería descender en climb
		        timeToImpactThreshold = 25.0; // tiempo amplio para reacción (20–30 s)
		        break;

		    case EFlyingStages.DESCENT:
		        minAGLThreshold = 1500.0;    // ≈ 5000 ft, inicio de alerta temprana
		        maxDescentRate = -5.0;       // permite descenso moderado (≈ -1000 ft/min)
		        timeToImpactThreshold = 30.0; // máximo margen para prevenir aproximación controlada al terreno
		        break;

		    case EFlyingStages.CRUISE:
		        minAGLThreshold = 2000.0;    // ≈ 6600 ft, vuelo de crucero sobre terreno alto
		        maxDescentRate = -1.0;       // casi sin descenso permitido en crucero
		        timeToImpactThreshold = 30.0; // margen máximo, ya que hay tiempo para actuar
		        break;

		    case EFlyingStages.TAKEOFF:
		        minAGLThreshold = 300.0;     // ≈ 1000 ft, primer punto crítico tras rotación
		        maxDescentRate = 0.0;        // no debería descender en takeoff
		        timeToImpactThreshold = 20.0; // tiempo suficiente para reacción en baja altura
		        break;

		    case EFlyingStages.LANDING:
		        minAGLThreshold = 100.0;     // ≈ 330 ft, justo antes de flare
		        maxDescentRate = -5.0;       // Permitimos hasta -1000 ft/min
		        timeToImpactThreshold = 20.0; // margen para corregir aproximación inestable
		        break;
		    default:
		        // Para otras fases, mantenemos los valores por defecto
		        break;
		        
		        
		    }


		    // Si estamos descendiendo (o con tasa negativa en ascenso/crucero)
		    if (verticalSpeed < maxDescentRate && agl < minAGLThreshold) {
		        double timeToImpact = agl / -verticalSpeed;

		        if (timeToImpact < timeToImpactThreshold) {
		        				            
		        	   if (this.getNotificationService() != null &&  this.getNotificationService().isMechanismAvailable("TAWS")) {
				         
			                this.getNotificationService().notify("⚠️ ALERTA TAWS: TERRAIN — PULL UP", "TAWS");
			                
			                return true; // Activamos alerta TAWS
			            }
		        }
		    }
		    
		    return false; // No se activó alerta TAWS
		}
	private boolean handleTakeoffPhase(double pitch) {
	    boolean correctionDone = false;
	    double currentSpeed = this.getSpeedSensor().getSpeedGS(); // Ground speed
	    final double ROTATION_SPEED = 38.0; 
	    if (currentSpeed >= ROTATION_SPEED) {
	        // Now it's safe to pitch up
	        final double SAFE_PITCH_MIN = 9.0;
	        final double SAFE_PITCH_MAX = 10.0;
	        correctionDone |= correctPitchIfNeeded(pitch, SAFE_PITCH_MIN, SAFE_PITCH_MAX, "TAKEOFF");
	    } else {
	        // Keep pitch neutral before rotation speed
	        this.getControlSurfaces().setElevatorDeflection(0.0);
	        logger.info(String.format("TAKEOFF: Waiting for rotation speed (current=%.2f m/s), keeping elevator neutral.", currentSpeed));
	    }


	    // Full thrust during takeoff
	    if (this.getFADEC().getCurrentThrust() < 91.0) {
	        this.getFADEC().setTHRUSTPercentage(91.0);
	        logger.info("TAKEOFF: Setting full thrust (91%).");
	        correctionDone = true;
	    }

	    // Ensure airbrakes are retracted
	    if (this.getControlSurfaces().getAirbrakeDeployment() > 0.0) {
	        this.getControlSurfaces().setAirbrakeDeployment(0.0);
	        logger.info("TAKEOFF: Retracting airbrakes.");
	        correctionDone = true;
	    }


	    return correctionDone;
	}
	
	public boolean handleObjectsProximity(boolean objectDetected) {
		if(objectDetected) {
			this.getProximitySensor().setObjectDetected(false); // Indica que se ha detectado un objeto cercano
			return true; // Indica que se ha detectado un objeto cercano
		}
		return false; // No se ha detectado ningún objeto cercano
	}

	/*private boolean handleLandingPhase(double pitch) {
	    boolean correctionDone = false;
	    double altitude = altimeterSensor.getAltitude();
	    double distanceToRunway = this.getNavigationSystem().getTotalDistance() - this.getNavigationSystem().getCurrentDistance();
	    boolean onGround = radioAltimeterSensor.isOnGround();
	    double currentThrust = this.getFADEC().getCurrentThrust();

	    if (!onGround) {
	        // --- FINAL APPROACH: last 150m before runway ---
	        if (distanceToRunway <= 150.0) {
	            // Target a gentle descent to 0m at runway threshold
	            double targetAltitude = Math.max(0.0, (altitude * distanceToRunway) / 150.0); // Linear descent
	            double altitudeError = targetAltitude - altitude;
	            double desiredPitch = clamp(-3.0, -1.0, -5.0); // Gentle nose down
	            double pitchError = desiredPitch - pitch;
	            double correctionTime = PlaneSimulationElement.getTimeStep() * 1.2;
	            double densityFactor = this.AHRSSensor.calculateDensityFactor(this.weatherSensor);
	            double desiredPitchRate = pitchError / correctionTime;
	            double requiredDeflection = Math.abs(pitchError) < 0.1 ? 0.0 : desiredPitchRate / (0.6 * densityFactor);
	            requiredDeflection = clamp(requiredDeflection, 15.0, -15.0);
	            this.getControlSurfaces().setElevatorDeflection(requiredDeflection);

	            // Reduce speed for touchdown
	            double speed = this.getSpeedSensor().getSpeedGS();
	            double targetSpeed = 55.0 + 5.0 * (distanceToRunway / 150.0); // from 60 m/s to 55 m/s
	            double speedError = targetSpeed - speed;
	            double thrustCorrection = currentThrust + 1.2 * speedError;
	            thrustCorrection = clamp(thrustCorrection, 30.0, 0.0);
	            this.getFADEC().setTHRUSTPercentage(thrustCorrection);

	            // Deploy airbrakes slightly if too fast
	            double airbrake = (speed > targetSpeed + 2.0) ? 0.3 : 0.0;
	            this.getControlSurfaces().setAirbrakeDeployment(airbrake);

	            correctionDone = true;
	        }
	        // Else: let the descent logic handle the rest
	    } else {
	        // --- ON GROUND: brake, cut thrust, align ---
	        final double TARGET_PITCH = 0.0;
	        double pitchError = TARGET_PITCH - pitch;
	        double timeStep = PlaneSimulationElement.getTimeStep();
	        double correctionTime = timeStep * 1.2;
	        double densityFactor = this.AHRSSensor.calculateDensityFactor(this.weatherSensor);
	        double desiredPitchRate = pitchError / correctionTime;
	        double requiredElevatorDeflection = desiredPitchRate / (0.6 * densityFactor);
	        requiredElevatorDeflection = clamp(requiredElevatorDeflection, 15.0, -15.0);

	        if (Math.abs(pitchError) < 0.1) {
	            this.getControlSurfaces().setElevatorDeflection(0.0);
	        } else {
	            this.getControlSurfaces().setElevatorDeflection(requiredElevatorDeflection);
	        }
	        correctionDone = true;

	        if (currentThrust > 0.0) {
	            this.getFADEC().setTHRUSTPercentage(0.0);
	            correctionDone = true;
	        }

	        double currentAirbrakeDeployment = this.getControlSurfaces().getAirbrakeDeployment();
	        if (currentAirbrakeDeployment < 0.9) {
	            this.getControlSurfaces().setAirbrakeDeployment(0.9);
	            correctionDone = true;
	        }

	        // Yaw alignment (as before)
	        double currentYaw = this.AHRSSensor.getYaw();
	        double runwayHeading = this.getLandingSystem().getRunwayHeadingDegrees();
	        double yawError = runwayHeading - currentYaw;
	        if (yawError > 180.0) yawError -= 360.0;
	        if (yawError < -180.0) yawError += 360.0;
	        double correctionTimeYaw = timeStep * 1.2;
	        double desiredYawRate = yawError / correctionTimeYaw;
	        double yawDensityFactor = this.AHRSSensor.calculateDensityFactor(this.weatherSensor);
	        double requiredRudderDeflection = desiredYawRate / (0.4 * yawDensityFactor);
	        requiredRudderDeflection = clamp(requiredRudderDeflection, 20.0, -20.0);

	        if (Math.abs(yawError) > LandingSystem.ALIGNMENT_TOLERANCE_DEGREES) {
	            this.getControlSurfaces().setRudderDeflection(requiredRudderDeflection);
	            correctionDone = true;
	        } else {
	            this.getControlSurfaces().setRudderDeflection(0.0);
	        }
	    }
	    return correctionDone;
	}*/

	

	public void adjustPitchThrustToMaintainAltitudeAndSpeedCruise() {
      

	    double altitude = altimeterSensor.getAltitude(); 
	    double pitch = this.getAHRSSensor().getPitch();
	    double thrust = this.getFADEC().getCurrentThrust();
	    double speed = this.getSpeedSensor().getSpeedGS();
	    double correctionTime = PlaneSimulationElement.getTimeStep()  * 2;
	    double densityFactor = this.getAHRSSensor().calculateDensityFactor(weatherSensor);
	    
	    
	    double SPEED_TARGET = 235.0; // 850 km/h ≈ 236 m/s
	    double SPEED_TOLERANCE = 5.8; // 10 km/h ≈ 2.8 m/s
	    
	    if(altitude < 10000) {
	    	 SPEED_TARGET = 250.0; // 900 km/h ≈ 250 m/s
	 	     SPEED_TOLERANCE = 5.8; // 10 km/h ≈ 2.8 m/s
	    }
	   

	    // Control de altitud (igual que antes)
	    if (altitude >= 10000.0 && altitude <= 11000.0) {
	        double desiredPitch = 0.0;
	        double pitchError = desiredPitch - pitch;
	        double desiredPitchRate = pitchError / correctionTime;
	        double requiredDeflection = desiredPitchRate / (0.6 * densityFactor);
	        requiredDeflection = clamp(requiredDeflection, 15.0, -15.0);

	        if (Math.abs(pitchError) < 0.1) {
	            this.getControlSurfaces().setElevatorDeflection(0.0);
	            logger.info("Pitch already close to 0°, elevator neutral.");
	        } else {
	            this.getControlSurfaces().setElevatorDeflection(requiredDeflection);
	            logger.info(String.format(
	                "Altitude stable, correcting pitch to 0°: pitch=%.2f°, deflection=%.2f°",
	                pitch, requiredDeflection
	            ));
	        }
	    } else {
	        double pitchCorrectionDirection = (altitude < 10000.0) ? 1.0 : -1.0;
	        if (pitchCorrectionDirection > 0) {
	            // Ascend: check if pitch is already in [12, 13]
	            if (pitch >= 12.0 && pitch <= 13.0) {
	                this.getControlSurfaces().setElevatorDeflection(0.0);
	                logger.info("Pitch within ascent range (12–13°), elevator neutral.");
	            } else {
	                double desiredPitch = clamp(pitch + 1.5, 13.0, 12.0);
	                double pitchError = desiredPitch - pitch;
	                double desiredPitchRate = pitchError / correctionTime;
	                double requiredDeflection = desiredPitchRate / (0.6 * densityFactor);
	                requiredDeflection = clamp(requiredDeflection, 15.0, -15.0);
	                this.getControlSurfaces().setElevatorDeflection(requiredDeflection);
	                logger.info(String.format(
	                    "Cruise Altitude Correction (ascend): currentAlt=%.1f m, pitch=%.2f°, desiredPitch=%.2f°, deflection=%.2f°",
	                    altitude, pitch, desiredPitch, requiredDeflection
	                ));
	            }
	        } else {
	            // Descend: check if pitch is already in [-10, -9]
	            if (pitch >= -10.0 && pitch <= -9.0) {
	                this.getControlSurfaces().setElevatorDeflection(0.0);
	                logger.info("Pitch within descent range (-10 to -9°), elevator neutral.");
	            } else {
	                double desiredPitch = clamp(pitch - 1.5, -9.0, -10.0);
	                double pitchError = desiredPitch - pitch;
	                double desiredPitchRate = pitchError / correctionTime;
	                double requiredDeflection = desiredPitchRate / (0.6 * densityFactor);
	                requiredDeflection = clamp(requiredDeflection, 15.0, -15.0);
	                this.getControlSurfaces().setElevatorDeflection(requiredDeflection);
	                logger.info(String.format(
	                    "Cruise Altitude Correction (descend): currentAlt=%.1f m, pitch=%.2f°, desiredPitch=%.2f°, deflection=%.2f°",
	                    altitude, pitch, desiredPitch, requiredDeflection
	                ));
	            }
	        }
	    }
	    // --- NUEVO: Control simple de velocidad ---
	 // --- Thrust correction to maintain target speed ---
	    final double MAX_THRUST = 90.0;
	    final double MIN_THRUST = 20.0;
	    double airBrakeLevel = this.getControlSurfaces().getAirbrakeDeployment();
	    double pitchRad = Math.toRadians(pitch);
	    double thrustEfficiency = 1.0 - Math.min(altitude / 15000.0, 0.3);
	    double airDensity = this.getWeatherSensor().getAirDensity();
	    double effectiveCd = SpeedSensor.DRAG_COEFFICIENT + (SpeedSensor.AIR_BRAKE_DRAG_COEFFICIENT * airBrakeLevel);
	    double dragForce = 0.5 * effectiveCd * airDensity * SpeedSensor.FRONTAL_AREA * Math.pow(speed, 2);
	    double groundForces = 0.0;
	    if (this.getRadioAltemeterSensor().isOnGround()) {
	        double rollingFrictionCoefficient = 0.03;
	        groundForces += rollingFrictionCoefficient * SpeedSensor.PLANE_MASS * 9.81;
	        if (airBrakeLevel > 0.7) {
	            groundForces += 0.25 * SpeedSensor.PLANE_MASS * 9.81;
	        }
	        if (thrust == 0) {
	            groundForces += 0.2 * SpeedSensor.MAX_THRUST_FORCE;
	        }
	    }

	    // Compute required thrust to maintain speed
	    double thrustHorizontal = dragForce + groundForces;
	    double thrustTotal = thrustHorizontal / Math.max(0.01, Math.cos(pitchRad));
	    double requiredThrust = (thrustTotal / Math.max(0.01, thrustEfficiency)) / SpeedSensor.MAX_THRUST_FORCE * 100.0;
	    requiredThrust = Math.max(MIN_THRUST, Math.min(MAX_THRUST, requiredThrust));

	    // Speed error for adjustment
	    double speedError = SPEED_TARGET - speed;
	    double thrustCorrection = requiredThrust + 1.2 * speedError;
	    thrustCorrection = Math.max(MIN_THRUST, Math.min(MAX_THRUST, thrustCorrection));

	    // --- Thrust smoothing ---
	    double maxThrustChangePerSec = 2.0; // max % thrust change per second
	    double maxChangeThisStep = maxThrustChangePerSec * PlaneSimulationElement.getTimeStep();
	    double thrustDelta = thrustCorrection - thrust;
	    thrustDelta = clamp(thrustDelta, maxChangeThisStep, -maxChangeThisStep);
	    double smoothedThrust = thrust + thrustDelta;
	    smoothedThrust = Math.max(MIN_THRUST, Math.min(MAX_THRUST, smoothedThrust));
	    this.getFADEC().setTHRUSTPercentage(smoothedThrust);

	    // Airbrake control
	    double targetAirbrake = 0.0;
	    if (speed > SPEED_TARGET + SPEED_TOLERANCE) {
	        targetAirbrake = Math.min(0.2, 0.05 * (speed - SPEED_TARGET));
	    }

	    double currentAirbrake = this.getControlSurfaces().getAirbrakeDeployment();
	    double maxAirbrakeChangePerSec = 0.1; // por segundo
	    double maxAirbrakeChangeThisStep = maxAirbrakeChangePerSec * PlaneSimulationElement.getTimeStep();
	    double airbrakeDelta = clamp(targetAirbrake - currentAirbrake, maxAirbrakeChangeThisStep, -maxAirbrakeChangeThisStep);
	    double smoothedAirbrake = clamp(currentAirbrake + airbrakeDelta, 1.0, 0.0);
	    this.getControlSurfaces().setAirbrakeDeployment(smoothedAirbrake);


	    // Control aerofrenos para evitar descensos bruscos (igual que antes)
	    if (altitude > 11000.0 && this.getControlSurfaces().getAirbrakeDeployment() > 0.3) {
	        this.getControlSurfaces().setAirbrakeDeployment(0.3);
	        logger.info("Reducing airbrakes to prevent excess descent.");
	    } else if (altitude >= 10000.0 && altitude <= 11000.0) {
	        // Within cruise band, retract airbrakes
	        this.getControlSurfaces().setAirbrakeDeployment(0.0);
	    }
	}
	private boolean checkServices() {
		 IAOASensor aoaSensor = OSGiUtils.getService(context, IAOASensor.class);
		 IAttitudeSensor attitudeSensor = OSGiUtils.getService(context, IAttitudeSensor.class);
		 IControlSurfaces controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
		 IAltitudeSensor altimeterSensor = OSGiUtils.getService(context, IAltitudeSensor.class);
		 ISpeedSensor speedSensor = OSGiUtils.getService(context, ISpeedSensor.class);
		 IRadioAltimeterSensor radioAltimeterSensor = 	OSGiUtils.getService(context, IRadioAltimeterSensor.class);
		 IFADEC fadec = OSGiUtils.getService(context, IFADEC.class);
		 INavigationSystem navigationSystem = OSGiUtils.getService(context, INavigationSystem.class);
		 IWeatherSensor weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
		 IFuelSensor fuelSensor = OSGiUtils.getService(context, IFuelSensor.class);
		 IEGTSensor egtSensor = OSGiUtils.getService(context, IEGTSensor.class);
		 ILandingSystem landingSystem = OSGiUtils.getService(context, ILandingSystem.class);
		 IProximitySensor proximitySensor = OSGiUtils.getService(context, IProximitySensor.class);
		 INotificationService notificationService = OSGiUtils.getService(context, INotificationService.class);
	    return aoaSensor != null &&
	           attitudeSensor != null &&
	           controlSurfaces != null &&
	           altimeterSensor != null &&
	           speedSensor != null &&
	           radioAltimeterSensor != null &&
	           fadec != null &&
	           navigationSystem != null &&
	           weatherSensor != null &&
	           fuelSensor != null &&
	           egtSensor != null &&
	           landingSystem != null &&
	           notificationService != null &&
	           proximitySensor != null;
	}
}
