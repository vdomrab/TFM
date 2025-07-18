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
		boolean correction_required = false;

		// Actualizacion de los angulos

		double pitch = this.AHRSSensor.getPitch();
        double roll = this.AHRSSensor.getRoll();
        double yaw = this.AHRSSensor.getYaw();
		
		
		if(this.getStabilityModeActive() ) {
	        double SAFE_PITCH_MIN = -4.9;
	        double SAFE_PITCH_MAX = 9.9;
	        double SAFE_ROLL_MIN = -8;
	        double SAFE_ROLL_MAX = 8;
	        double MIN_ROLL_ERROR_THRESHOLD = 0.1; // degrees
	        double correctionTime = PlaneSimulationElement.getTimeStep() * 2;

	        if (roll >= SAFE_ROLL_MIN && roll <= SAFE_ROLL_MAX) {
	            this.getControlSurfaces().setAileronDeflection(0.0);
	            logger.info("Roll within safe range, aileron neutral.");
	        } else {
	            double densityFactor = this.getAHRSSensor().calculateDensityFactor(this.getWeatherSensor());
	            double desiredRoll;
	            if (roll > SAFE_ROLL_MAX) {
	                // Too much positive roll, correct negatively
	                desiredRoll = clamp(roll - 1.5, SAFE_ROLL_MAX, SAFE_ROLL_MIN);
	            } else {
	                // Too much negative roll, correct positively
	                desiredRoll = clamp(roll + 1.5, SAFE_ROLL_MAX, SAFE_ROLL_MIN);
	            }
	            double rollError = desiredRoll - roll;
	            double desiredRollRate = rollError / correctionTime;
	            double requiredDeflection = desiredRollRate / (1.0 * densityFactor);
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
	        if (this.navigationSystem.getCurrentFlyghtStage() == EFlyingStages.CLIMB) {
	            SAFE_PITCH_MIN = 11.9;
	            SAFE_PITCH_MAX = 15.1;
	            boolean unstablePitch = pitch < SAFE_PITCH_MIN || pitch > SAFE_PITCH_MAX;
	            if (unstablePitch) {
	                correction_required = true;
	                double densityFactor = this.getAHRSSensor().calculateDensityFactor(this.getWeatherSensor());
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
	                double requiredDeflection = desiredPitchRate / (0.6 * densityFactor);

	                if (Math.abs(pitchError) < MIN_ROLL_ERROR_THRESHOLD) {
	                    this.getControlSurfaces().setElevatorDeflection(0.0);
	                } else {
	                    logger.info(String.format("CLIMB: pitch correction -> current=%.2f°, error=%.2f°, deflection=%.2f°, densityFactor=%.3f",
	                        pitch, pitchError, requiredDeflection, densityFactor));
	                    logger.info("Correcting Pitch angle...");
	                    this.getControlSurfaces().setElevatorDeflection(requiredDeflection);
	                }
	            } else {
	                this.getControlSurfaces().setElevatorDeflection(0.0);
	            }

	            // Asegurar thrust correcto
	            if (this.getFADEC().getCurrentThrust() < 85.0) {
	                this.getFADEC().setTHRUSTPercentage(85.0);
	                logger.info("CLIMB: increasing thrust to 85%");
	            }

	            // Retraer frenos de aire si están activos
	            if (this.getControlSurfaces().getAirbrakeDeployment() > 0.0) {
	                this.getControlSurfaces().setAirbrakeDeployment(0.0);
	                logger.info("CLIMB: retracting airbrakes.");
	            }
			} else if(this.navigationSystem.getCurrentFlyghtStage() == EFlyingStages.DESCENT) {
				SAFE_PITCH_MIN = -15.0;
			    SAFE_PITCH_MAX = 10.0;

			    boolean unstablePitch = pitch < SAFE_PITCH_MIN || pitch  > SAFE_PITCH_MAX;
			    if (unstablePitch) {
			        correction_required = true;
			        double densityFactor = this.getAHRSSensor().calculateDensityFactor(this.getWeatherSensor());
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
			        double requiredDeflection = desiredPitchRate / (0.6 * densityFactor);

			        if (Math.abs(pitchError) < MIN_ROLL_ERROR_THRESHOLD) {
			            this.getControlSurfaces().setElevatorDeflection(0.0);
			        } else {
			            logger.info(String.format("DESCENT: pitch correction -> current=%.2f°, error=%.2f°, deflection=%.2f°, densityFactor=%.3f",
			                pitch, pitchError, requiredDeflection, densityFactor));
			            logger.info("Correcting Pitch angle...");
			            this.getControlSurfaces().setElevatorDeflection(requiredDeflection);
			        }
			    } else {
			        this.getControlSurfaces().setElevatorDeflection(0.0);
			    }

		         // Thrust para descenso 
	            if(this.getFADEC().getCurrentThrust() < 20 || this.getFADEC().getCurrentThrust() > 30) {
	                this.getFADEC().setTHRUSTPercentage(30);
	                logger.info("Correcting Thrust...");// Aumentar potencia en ascenso
	            }
	            
	            // Ajuste de frenos de aire para asistencia en descenso
	            if(this.getControlSurfaces().getAirbrakeDeployment() < 0.5 
	            		|| this.getControlSurfaces().getAirbrakeDeployment() > 0.75) {
					this.getControlSurfaces().setAirbrakeDeployment(0.5); // Asistencia en descenso
					logger.info("Adjusting airbrakes for descent assistance...");	            
				}
	            
    	        
			} else if(this.navigationSystem.getCurrentFlyghtStage() == EFlyingStages.CRUISE) {
				adjustPitchThrustToMaintainAltitudeAndSpeedCruise();
			}
			
			//Stall warning
			if ( this.getAOASensor().getAOA() > 12 && this.getAOASensor().getAOA() < 15 ) {
				correction_required = true;
				logger.info("Plane approaching stall condition");
				if ( this.getNotificationService() != null )
					this.getNotificationService().notify("Plane approaching stall condition: " + this.getAOASensor().getAOA() );
			}else if (this.getAOASensor().getAOA() >= 15) {
				correction_required = true;
				logger.info("Plane is in stall condition");
				if ( this.getNotificationService() != null  )
					this.getNotificationService().notify("Plane is in stall condition: " + this.getAOASensor().getAOA() );
				if(this.getFallbackPlan() != null) {
					this.activateTheFallbackPlan();
				} else {
					logger.error("No fallback plan available to handle stall condition.");
				}
			
			}
			
			
			if ( !correction_required ) {
				logger.info("Monitoring flying parameters. Nothing to warn ...");
			}
			
			
		}
		return this;
	}

	private double clamp(double rate, double maxRate, double min_rate) {
	    return Math.max(min_rate, Math.min(rate, maxRate));
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


