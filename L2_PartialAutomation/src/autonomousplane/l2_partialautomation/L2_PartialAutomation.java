package autonomousplane.l2_partialautomation;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IL2_PartialAutomation;
import autonomousplane.infraestructure.autopilot.L2_FlyingService;
import autonomousplane.infraestructure.devices.SpeedSensor;
import autonomousplane.infraestructure.devices.WeatherSensor;
import autonomousplane.interfaces.EFlyingStages;
import autonomousplane.simulation.simulator.IPlaneSimulation;
import autonomousplane.simulation.simulator.PlaneSimulationElement;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
import autonomousplane.infraestructure.OSGiUtils;

public class L2_PartialAutomation extends L2_FlyingService implements IL2_PartialAutomation {
	
	public L2_PartialAutomation(BundleContext context, String id) {
		super(context, id);
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setStabilityModeActive(true);
	}



	@Override
	public IFlyingService performTheFlyingFunction() {
	    logger.info("Performing partial automation...");
	    boolean correctionRequired = false;

	    double pitch = this.AHRSSensor.getPitch();
	    double roll = this.AHRSSensor.getRoll();

	    if (this.getStabilityModeActive()) {
	        correctionRequired |= correctRollIfNeeded(roll);

	        EFlyingStages stage = this.navigationSystem.getCurrentFlyghtStage();
	        switch (stage) {
	            case CLIMB:
	                correctionRequired |= handleClimbPhase(pitch);
	                break;
	            case DESCENT:
	                correctionRequired |= handleDescentPhase(pitch);
	                break;
	            case CRUISE:
	                adjustPitchThrustToMaintainAltitudeAndSpeedCruise();
	                break;
	            default:
	                break; // Other phases ignored here
	        }

	        correctionRequired |= checkTerrainAwareness(stage, radioAltimeterSensor.getGroundDistance(), altimeterSensor.getVerticalSpeed());
	        correctionRequired |= handleStallWarnings();

	        if (!correctionRequired) {
	            logger.info("Monitoring flying parameters. Nothing to warn ...");
	        }
	    }

	    return this;
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

	    if (this.getFADEC().getCurrentThrust() < 85.0) {
	        this.getFADEC().setTHRUSTPercentage(85.0);
	        logger.info("CLIMB: increasing thrust to 85%");
	        correctionDone = true;
	    }

	    if (this.getControlSurfaces().getAirbrakeDeployment() > 0.0) {
	        this.getControlSurfaces().setAirbrakeDeployment(0.0);
	        logger.info("CLIMB: retracting airbrakes.");
	        correctionDone = true;
	    }

	    return correctionDone;
	}

	private boolean handleDescentPhase(double pitch) {
	    final double SAFE_PITCH_MIN = -7.0;
	    final double SAFE_PITCH_MAX = -2.0;
	    boolean correctionDone = correctPitchIfNeeded(pitch, SAFE_PITCH_MIN, SAFE_PITCH_MAX, "DESCENT");

	    double currentThrust = this.getFADEC().getCurrentThrust();
	    if (currentThrust < 20 || currentThrust > 30) {
	        this.getFADEC().setTHRUSTPercentage(30.0);
	        logger.info("DESCENT: correcting thrust to 30%");
	    }

	    double airbrake = this.getControlSurfaces().getAirbrakeDeployment();
	    if (airbrake < 0.5 || airbrake > 0.75) {
	        this.getControlSurfaces().setAirbrakeDeployment(0.5);
	        logger.info("DESCENT: adjusting airbrakes for descent assistance.");
	    }

	    return correctionDone;
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

	private boolean handleStallWarnings() {
	    double aoa = this.getAOASensor().getAOA();
	    if (aoa > 12 && aoa < 15) {
	        notifyStallCondition("approaching stall condition", aoa);
	        return true;
	    } else if (aoa >= 15) {
	        notifyStallCondition("is in stall condition", aoa);
	        if (this.getFallbackPlan() != null) {
	            this.activateTheFallbackPlan();
	        } else {
	            logger.error("No fallback plan available to handle stall condition.");
	        }
	        return true;
	    }
	    return false;
	}

	private void notifyStallCondition(String message, double aoa) {
	    logger.info("Plane " + message);
	    if (this.getNotificationService() != null && this.getNotificationService().isMechanismAvailable("StallWarning")) {
	        this.getNotificationService().notify("Plane " + message + ": " + aoa);
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
		        minAGLThreshold = 600.0;    // margen amplio para evitar falsas alertas
		        maxDescentRate = 0.0;       // no debería descender en climb
		        timeToImpactThreshold = 12.0;
		        break;

		    case EFlyingStages.DESCENT:
		        minAGLThreshold = 500.0;    // umbral razonable para alerta en descenso
		        maxDescentRate = -5.0;      // permite descenso moderado (≈ -1000 ft/min)
		        timeToImpactThreshold = 18.0;
		        break;

		    case EFlyingStages.CRUISE:
		        minAGLThreshold = 1000.0;   // mínimo AGL esperado en crucero
		        maxDescentRate = -1.0;      // casi sin descenso permitido en crucero
		        timeToImpactThreshold = 10.0;
		        break;

		    case EFlyingStages.TAKEOFF:
		        minAGLThreshold = 150.0;    // cercano al suelo en takeoff
		        maxDescentRate = 0.0;       // no debería descender en takeoff
		        timeToImpactThreshold = 10.0;
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
				         
			                this.getNotificationService().notify("⚠️ ALERTA TAWS: TERRAIN — PULL UP");
			                
			                return true; // Activamos alerta TAWS
			            }
		        }
		    }
		    
		    return false; // No se activó alerta TAWS
		}
	
	public void adjustPitchThrustToMaintainAltitudeAndSpeedCruise() {
      

	    double altitude = altimeterSensor.getAltitude(); 
	    double pitch = this.getAHRSSensor().getPitch();
	    double thrust = this.getFADEC().getCurrentThrust();
	    double speed = this.getSpeedSensor().getSpeedGS();
	    double correctionTime = PlaneSimulationElement.getTimeStep()  * 2;
	    double densityFactor = this.getAHRSSensor().calculateDensityFactor(weatherSensor);

	    double SPEED_TARGET = 236.0; // 850 km/h ≈ 236 m/s
	    double SPEED_TOLERANCE = 5.8; // 10 km/h ≈ 2.8 m/s

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

	    // Required thrust to keep acceleration = 0
	    double thrustHorizontal = dragForce + groundForces;
	    double thrustTotal = thrustHorizontal / Math.max(0.01, Math.cos(pitchRad));
	    double requiredThrust = (thrustTotal / Math.max(0.01, thrustEfficiency)) / SpeedSensor.MAX_THRUST_FORCE * 100.0;
	    requiredThrust = Math.max(MIN_THRUST, Math.min(MAX_THRUST, requiredThrust));

	    // Correction logic
	    if (speed >= SPEED_TARGET - SPEED_TOLERANCE && speed <= SPEED_TARGET + SPEED_TOLERANCE) {
	        this.getFADEC().setTHRUSTPercentage(requiredThrust);
	        this.getControlSurfaces().setAirbrakeDeployment(0.0);
	    } else {
	        double speedError = SPEED_TARGET - speed;
	        double thrustCorrection = requiredThrust + 1.2 * speedError;
	        thrustCorrection = Math.max(MIN_THRUST, Math.min(MAX_THRUST, thrustCorrection));
	        this.getFADEC().setTHRUSTPercentage(thrustCorrection);

	        double airbrakeCommand = 0.0;
	        if (speed > SPEED_TARGET + SPEED_TOLERANCE) {
	            airbrakeCommand = Math.min(0.7, 0.05 * (speed - SPEED_TARGET));
	        }
	        this.getControlSurfaces().setAirbrakeDeployment(airbrakeCommand);

	        logger.info(String.format(
	            "Speed: %.2f m/s, Thrust: %.1f%%, Airbrake: %.2f",
	            speed, thrustCorrection, airbrakeCommand
	        ));
	    }


	    // Control aerofrenos para evitar descensos bruscos (igual que antes)
	    if (altitude > 11000.0 && this.getControlSurfaces().getAirbrakeDeployment() > 0.3) {
	        this.getControlSurfaces().setAirbrakeDeployment(0.3);
	        logger.info("Reducing airbrakes to prevent excess descent.");
	    } else if (altitude >= 10000.0 && altitude <= 11000.0) {
	        // Within cruise band, retract airbrakes
	        this.getControlSurfaces().setAirbrakeDeployment(0.0);
	    }
	}
}


