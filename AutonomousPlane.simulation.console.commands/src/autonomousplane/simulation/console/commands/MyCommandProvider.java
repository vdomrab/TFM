package autonomousplane.simulation.console.commands;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IL0_ManualNavigation;
import autonomousplane.autopilot.interfaces.IL1_BasicNavigationAssistance;
import autonomousplane.autopilot.interfaces.IL1_FlyingService;
import autonomousplane.autopilot.interfaces.IL2_FlyingService;
import autonomousplane.autopilot.interfaces.IL2_PartialAutomation;
import autonomousplane.autopilot.interfaces.IL3_AdvancedAutomation;
import autonomousplane.autopilot.interfaces.IL3_FlyingService;
import autonomousplane.autopilot.interfaces.IL3_IntenseWeatherNavigation;
import autonomousplane.autopilot.interfaces.IL3_LowFuelConsumption;
import autonomousplane.devices.interfaces.*;
import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.autopilot.L1_FlyingService;
import autonomousplane.infraestructure.autopilot.L2_FlyingService;
import autonomousplane.infraestructure.autopilot.L3_FlyingService;
import autonomousplane.infraestructure.devices.AOASensor;
import autonomousplane.infraestructure.devices.ControlSurfaces;
import autonomousplane.infraestructure.devices.FuelSensor;
import autonomousplane.infraestructure.devices.RadioAltimeterSensor;
import autonomousplane.infraestructure.devices.SpeedSensor;
import autonomousplane.infraestructure.interaction.NotificationService;
import autonomousplane.infraestructure.interaction.StallWarning;
import autonomousplane.interaction.interfaces.IInteractionMechanism;
import autonomousplane.interaction.interfaces.INotificationService;
import autonomousplane.interfaces.EClimate;
import autonomousplane.interfaces.EFlyingStages;
import autonomousplane.interfaces.IIdentifiable;
import autonomousplane.l0_manualnavigation.L0_ManualNavigationARC;
import autonomousplane.simulation.simulator.IManualSimulatorStepsManager;
import autonomousplane.simulation.simulator.IPlaneSimulation;
import autonomousplane.simulation.simulator.PlaneSimulationElement;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.resources.adaptation.SelfConfigureProbe;
import es.upv.pros.tatami.adaptation.mapek.lite.resources.ARC.artifacts.components.arc.ProbeARC;
import es.upv.pros.tatami.osgi.utils.components.SearchTools;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;


public class MyCommandProvider {

	protected static SmartLogger logger = SmartLogger.getLogger(MyCommandProvider.class);

	BundleContext context = null;

	public MyCommandProvider(BundleContext context) {
		this.context = context;
	}
	
	public void show() {
	
		IAOASensor aoaSensor = OSGiUtils.getService(context, IAOASensor.class);
		IFlyingService flyingService = OSGiUtils.getService(context, IFlyingService.class);
		IAttitudeSensor attitudeSensor = OSGiUtils.getService(context, IAttitudeSensor.class);
		IControlSurfaces controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
		IAltitudeSensor altitudeSensor = OSGiUtils.getService(context, IAltitudeSensor.class);
		ISpeedSensor speedSensor = OSGiUtils.getService(context, ISpeedSensor.class);
		IRadioAltimeterSensor radioAltimeter = OSGiUtils.getService(context, IRadioAltimeterSensor.class);
		IFADEC fadec = OSGiUtils.getService(context, IFADEC.class);
		INavigationSystem gnss = OSGiUtils.getService(context, INavigationSystem.class,
		        "(" + IIdentifiable.ID + "=GNSS)");
		    if (gnss == null) {
		        gnss = OSGiUtils.getService(context, INavigationSystem.class,
		            "(" + IIdentifiable.ID + "=RadioNavigationSystem)");
		    }
		IWeatherSensor weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
		// Obtener el Landing System
		ILandingSystem landingSystem = OSGiUtils.getService(context, ILandingSystem.class,
		    "(" + IIdentifiable.ID + "=ILSSystem)");

		if (landingSystem == null) {
		    landingSystem = OSGiUtils.getService(context, ILandingSystem.class,
		        "(" + IIdentifiable.ID + "=SVLDSystem)");
		}

		IFuelSensor fuelSensor = OSGiUtils.getService(context, IFuelSensor.class,
			    "(" + IIdentifiable.ID + "=CapacitiveFuelSensor)");

			if (fuelSensor == null) {
			    fuelSensor = OSGiUtils.getService(context, IFuelSensor.class,
			        "(" + IIdentifiable.ID + "=FloatSensor)");
			}
		IEGTSensor egtSensor = OSGiUtils.getService(context, IEGTSensor.class);
		
		if (flyingService == null ) {
			System.out.println("Flying Service not available.");
			return;
		}
		System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("|                  PLANE INFO");
		System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println(String.format("|     Plane ID: %s", flyingService.getId()));
		System.out.println(String.format("|     Step-Speed : %s", PlaneSimulationElement.getTimeStep() ));
		if(flyingService instanceof IL0_ManualNavigation) {
			System.out.println("|     Navigation Mode: L0 - Manual Navigation");
		} else if (flyingService instanceof IL3_FlyingService) {
			if(flyingService instanceof IL3_AdvancedAutomation)
				System.out.println("|     Navigation Mode: L3 - ");
			if(flyingService instanceof IL3_LowFuelConsumption)
				System.out.println("|     Navigation Mode: L3 - Low Fuel Consumption");
			if(flyingService instanceof IL3_IntenseWeatherNavigation)
				System.out.println("|     Navigation Mode: L3 - Intense Weather Navigation");
			System.out.println(String.format("|     Autopilot status: %s", ((L3_FlyingService) flyingService).getStabilityModeActive() ? "Active" : "Inactive"));
		} else if (flyingService instanceof IL2_FlyingService) {
			System.out.println("|     Navigation Mode: L2 - Partial Automation");
			System.out.println(String.format("|     Autopilot status: %s", ((L2_FlyingService) flyingService).getStabilityModeActive() ? "Active" : "Inactive"));
		
		} else if (flyingService instanceof IL1_BasicNavigationAssistance) {
			System.out.println("|     Navigation Mode: L1 - Basic Navigation Assistance");
			System.out.println(String.format("|     Autopilot status: %s", ((L1_FlyingService) flyingService).getStabilityModeActive() ? "Active" : "Inactive"));
		} else {
			System.out.println("|     Navigation Mode: Unknown");
		}


		if ( aoaSensor != null ) {
			System.out.println("|                  AOA INFO");

			System.out.println(String.format("|     AOA Value: %s °", aoaSensor.getAOA()));
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");

		}
		if ( attitudeSensor != null ) {
			System.out.println("|                  AHRS INFO");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
			System.out.println("| | GYRO INFO |");
			System.out.println(String.format("|     Roll: %s °", attitudeSensor.getRoll()));
			System.out.println(String.format("|     Pitch: %s °", attitudeSensor.getPitch()));
			System.out.println(String.format("|     Yaw: %s °", attitudeSensor.getYaw()));
			System.out.println("| | ANGULAR RATE INFO |");
			System.out.println(String.format("|     Roll Rate: %s °/s", attitudeSensor.getRollRate()));
			System.out.println(String.format("|     Pitch Rate: %s °/s", attitudeSensor.getPitchRate()));
			System.out.println(String.format("|     Yaw Rate: %s °/s", attitudeSensor.getYawRate()));
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");

		}
		if (altitudeSensor != null && radioAltimeter != null) {
			System.out.println("|                  ALTITUDE INFO");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
			System.out.println(String.format("|     Altitude: %s m", altitudeSensor.getAltitude()));
			System.out.println(String.format("|     Ground Altitude (Real): %s m", radioAltimeter.getRealGroundAltitude()));	
			System.out.println(String.format("|     Ground Distance (Sensor): %s m", radioAltimeter.getGroundDistance()));	
			System.out.println(String.format("|     Altitude Speed: %s m/s", altitudeSensor.getVerticalSpeed()));
			System.out.println(String.format("|     Altitude Acceleration: %s m/s²", altitudeSensor.getVerticalAcceleration()));

			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");

		}
		
		if (fadec != null && speedSensor != null) {
			System.out.println("|                  Speed INFO");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
			System.out.println(String.format("|     Speed (TAS): %s m/s", speedSensor.getSpeedTAS()));
			System.out.println(String.format("|     Thrust: %s %%", fadec.getCurrentThrust()));
			System.out.println(String.format("|     Engine Status: %s", fadec.isFailure() ? "Failure" : "Normal"));
			System.out.println(String.format("|     Acceleration(TAS): %s m/s²", speedSensor.getSpeedIncreaseTAS()));
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");


		}
		if(fuelSensor != null && egtSensor != null) {
			System.out.println("|                  Engine INFO");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
			
			System.out.println(String.format("|     Fuel Level: %s kg", fuelSensor.getFuelLevel()));
			System.out.println(String.format("|     Fuel Level: %s %%", fuelSensor.getFuelPercentage()));
			System.out.println(String.format("|     Fuel Consumption Rate: %s kg/s", fuelSensor.getFuelConsumptionRate()));
				System.out.println(String.format("|     Estimated Endurance: %s s", fuelSensor.getEstimatedEnduranceSeconds()));
			if(speedSensor != null) {
				System.out.println(String.format("|     Estimated Range: %s m", fuelSensor.getEstimatedRangeMeters(speedSensor.getSpeedTAS())));
			}
			System.out.println(String.format("|     Engine Temperature: %s °C", egtSensor.getTemperature()));
			System.out.println(String.format("|     Cooling Enable: %s", egtSensor.isCoolingEnabled()));
			System.out.println(String.format("|     Heating Enable: %s", egtSensor.isHeatingEnabled()));
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");

		}
		if ( controlSurfaces != null ) {
			System.out.println("|                  CONTROL SURFACES INFO");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
			System.out.println(String.format("|     Aileron (Roll): %s °", controlSurfaces.getAileronDeflection()));
			System.out.println(String.format("|     Elevator (Pitch): %s °", controlSurfaces.getElevatorDeflection()));
			System.out.println(String.format("|     Rudder (Yaw): %s °", controlSurfaces.getRudderDeflection()));
			System.out.println(String.format("|     Airbrake: %s %%", controlSurfaces.getAirbrakeDeployment()*100));
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");

		}
		if(gnss != null) {
			System.out.println("|                  Navigation System INFO");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
			System.out.println(String.format("|     Total Distance: %s m", gnss.getTotalDistance()));
			System.out.println(String.format("|     Current Distance: %s m", gnss.getCurrentDistance()));
			System.out.println(String.format("|     Current Flyght Stage: %s", gnss.getCurrentFlyghtStage()));
			if(landingSystem != null) {
				System.out.println(String.format("|     Landing System: %s", landingSystem.getRunwayHeadingDegrees()));
			}
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");

		}
		if (weatherSensor != null) {
			System.out.println("|                  WEATHER INFO");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
			System.out.println(String.format("|     Weather Status: %s", weatherSensor.getActualClimate()));
			System.out.println(String.format("|     Temperature: %s °C", weatherSensor.getTemperature()));
			System.out.println(String.format("|     Ground Temperature: %s °C", weatherSensor.getGroundTemperature()));
			System.out.println(String.format("|     Air Density: %s kg/m³", weatherSensor.getAirDensity()));
			System.out.println(String.format("|     Humidity: %s %%", weatherSensor.getHumidity()));
			System.out.println(String.format("|     Pressure: %s hPa", weatherSensor.getHPA()));
			System.out.println(String.format("|     In Cloud: %s", weatherSensor.isInCloud()));
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void knowledge() {
	    IKnowledgeProperty kp_aoaValue = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("aoa_value");
	    IKnowledgeProperty kp_autonomusType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("autonomus_type");
	    IKnowledgeProperty kp_componentStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("component_status");
	    IKnowledgeProperty kp_fallbackPlan = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("fallback_plan_type");
	    IKnowledgeProperty kp_thermalStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("thermal_status");
	    IKnowledgeProperty kp_humidityLevel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("humidity_level");
	    IKnowledgeProperty kp_fuel_distance = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("fuel_distance");
	    IKnowledgeProperty kp_distanceRemaining = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("distance_remaining");
	    IKnowledgeProperty kp_weatherStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("weather_status");
	    IKnowledgeProperty kp_terrainAltitude = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("terrain_altitude");
	    IKnowledgeProperty kp_flyingStage = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("flying_stage");
	    IKnowledgeProperty kp_verticalSpeed = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("vertical_speed");
	    IKnowledgeProperty kp_fuelStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("fuel_status");
	    IKnowledgeProperty kp_engineFailureStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("engine_failure_status");
	    
	    String aoaValue = (kp_aoaValue != null && kp_aoaValue.getValue() != null) ? kp_aoaValue.getValue().toString() : "UNKNOWN";
	    String autonomusType = (kp_autonomusType != null && kp_autonomusType.getValue() != null) ? kp_autonomusType.getValue().toString() : "UNKNOWN";
	    String fallbackPlanType = (kp_fallbackPlan != null && kp_fallbackPlan.getValue() != null) ? kp_fallbackPlan.getValue().toString() : "UNKNOWN";
	    String thermalStatus = (kp_thermalStatus != null && kp_thermalStatus.getValue() != null) ? kp_thermalStatus.getValue().toString() : "UNKNOWN";
	    String humidityLevel = (kp_humidityLevel != null && kp_humidityLevel.getValue() != null) ? kp_humidityLevel.getValue().toString() : "UNKNOWN";
	    String fuelDistance = (kp_fuel_distance != null && kp_fuel_distance.getValue() != null) ? kp_fuel_distance.getValue().toString() : "UNKNOWN";
	    String distanceRemaining = (kp_distanceRemaining != null && kp_distanceRemaining.getValue() != null) ? kp_distanceRemaining.getValue().toString() : "UNKNOWN";
	    String weatherStatus = (kp_weatherStatus != null && kp_weatherStatus.getValue() != null) ? kp_weatherStatus.getValue().toString() : "UNKNOWN";
	    String terrainAltitude = (kp_terrainAltitude != null && kp_terrainAltitude.getValue() != null) ? kp_terrainAltitude.getValue().toString() : "UNKNOWN";
	    String flyingStage = (kp_flyingStage != null && kp_flyingStage.getValue() != null) ? kp_flyingStage.getValue().toString() : "UNKNOWN";
	    String verticalSpeed = (kp_verticalSpeed != null && kp_verticalSpeed.getValue() != null) ? kp_verticalSpeed.getValue().toString() : "UNKNOWN";
	    Set<String> componentStatusList = (kp_componentStatus != null && kp_componentStatus.getValue() != null) ? (Set<String>) kp_componentStatus.getValue() : new java.util.HashSet<>();
	    String fuel_status = (kp_fuelStatus != null && kp_fuelStatus.getValue() != null) ? kp_fuelStatus.getValue().toString() : "UNKNOWN";
	    String engineFailureStatus = (kp_engineFailureStatus != null && kp_engineFailureStatus.getValue() != null) ? ((boolean) kp_engineFailureStatus.getValue() ? "FAILURE" : "NORMAL") : "UNKNOWN";
	    int componentStatusCount = componentStatusList.size();

	    System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");
	    System.out.println("*  KNOWLEDGE");
	    System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");
	    System.out.println(String.format("*   aoa_value: %s", aoaValue));
	    System.out.println(String.format("*   autonomus_type: %s", autonomusType));
	    System.out.println(String.format("*   fallback_plan_type: %s", fallbackPlanType));
	    System.out.println(String.format("*   thermal_status: %s", thermalStatus));
	    System.out.println(String.format("*   humidity_level: %s", humidityLevel));
	    System.out.println(String.format("*   (remaining distance) fuel_distance: %s", fuelDistance));
	    System.out.println(String.format("*   fuel_status: %s", fuel_status));
	    System.out.println(String.format("*   distance_remaining: %s", distanceRemaining));
	    System.out.println(String.format("*   weather_status: %s", weatherStatus));
	    System.out.println(String.format("*   terrain_altitude: %s", terrainAltitude));
	    System.out.println(String.format("*   flying_stage: %s", flyingStage));
	    System.out.println(String.format("*   vertical_speed: %s", verticalSpeed));
	    System.out.println(String.format("*   component_status: %s", componentStatusList));
	    System.out.println(String.format("*   engine_status: %s", engineFailureStatus));
	    System.out.println(String.format("*   component_status_count: %d", componentStatusCount));
	    System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");
	}

	
	public void initialize() {
		String sondaFilter = String.format("(%s=%s)", IIdentifiable.ID,  SelfConfigureProbe.ID);
		IAdaptiveReadyComponent laSondaARC = SearchTools.doSearch(this.context, IAdaptiveReadyComponent.class, sondaFilter);
		SelfConfigureProbe laSonda = (SelfConfigureProbe) laSondaARC.getServiceSupply(ProbeARC.SUPPLY_PROBESERVICE);
		laSonda.sendSelfConfigureRequest();
	}


	public void aoa(String property, double value) {
		IAOASensor aoaSensor = OSGiUtils.getService(context, IAOASensor.class);
		if (aoaSensor == null) {
			System.out.println("AOA Sensor not available.");
			return;
		}
		
		if (property.equalsIgnoreCase("set")) {
			try {
				double aoaValue = value; // Assuming value is already a double
				aoaSensor.setAOA(aoaValue);
				System.out.println("AOA set to: " + aoaValue);
			} catch (NumberFormatException e) {
				System.out.println("Invalid AOA value: " + value);
			}
		} else if (property.equalsIgnoreCase("get")) {
			System.out.println("Current AOA: " + aoaSensor.getAOA());
		} else {
			System.out.println("Unknown property: " + property);
		}
		
		
		
		
	}
	
	public void controlSurface(String surface, double value) {
	    IControlSurfaces controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
	    if (controlSurfaces == null) {
	        System.out.println("Control Surfaces not available.");
	        return;
	    }

	    switch (surface.toLowerCase()) {
	        case "aileron":
	            if (value > ControlSurfaces.MAX_AILERON_DEFLECTION) {
	                System.out.println("Aileron deflection too high (" + value + "), limiting to max " + ControlSurfaces.MAX_AILERON_DEFLECTION);
	                value = ControlSurfaces.MAX_AILERON_DEFLECTION;
	            } else if (value < -ControlSurfaces.MAX_AILERON_DEFLECTION) {
	                System.out.println("Aileron deflection too low (" + value + "), limiting to min " + (-ControlSurfaces.MAX_AILERON_DEFLECTION));
	                value = -ControlSurfaces.MAX_AILERON_DEFLECTION;
	            }
	            controlSurfaces.setAileronDeflection(value);
	            System.out.println("Aileron set to: " + value);
	            break;

	        case "elevator":
	            if (value > ControlSurfaces.MAX_ELEVATOR_DEFLECTION) {
	                System.out.println("Elevator deflection too high (" + value + "), limiting to max " + ControlSurfaces.MAX_ELEVATOR_DEFLECTION);
	                value = ControlSurfaces.MAX_ELEVATOR_DEFLECTION;
	            } else if (value < -ControlSurfaces.MAX_ELEVATOR_DEFLECTION) {
	                System.out.println("Elevator deflection too low (" + value + "), limiting to min " + (-ControlSurfaces.MAX_ELEVATOR_DEFLECTION));
	                value = -ControlSurfaces.MAX_ELEVATOR_DEFLECTION;
	            }
	            controlSurfaces.setElevatorDeflection(value);
	            System.out.println("Elevator set to: " + value);
	            break;

	        case "rudder":
	            if (value > ControlSurfaces.MAX_RUDDER_DEFLECTION) {
	                System.out.println("Rudder deflection too high (" + value + "), limiting to max " + ControlSurfaces.MAX_RUDDER_DEFLECTION);
	                value = ControlSurfaces.MAX_RUDDER_DEFLECTION;
	            } else if (value < -ControlSurfaces.MAX_RUDDER_DEFLECTION) {
	                System.out.println("Rudder deflection too low (" + value + "), limiting to min " + (-ControlSurfaces.MAX_RUDDER_DEFLECTION));
	                value = -ControlSurfaces.MAX_RUDDER_DEFLECTION;
	            }
	            controlSurfaces.setRudderDeflection(value);
	            System.out.println("Rudder set to: " + value);
	            break;

	        case "airbrake":
	            double deployment = value / 100.0; // convertir porcentaje a 0.0 - 1.0
	            if (deployment > ControlSurfaces.MAX_AIRBRAKE_DEPLOYMENT) {
	                System.out.println("Airbrake deployment too high (" + deployment + "), limiting to max " + ControlSurfaces.MAX_AIRBRAKE_DEPLOYMENT);
	                deployment = ControlSurfaces.MAX_AIRBRAKE_DEPLOYMENT;
	            } else if (deployment < 0.0) {
	                System.out.println("Airbrake deployment too low (" + deployment + "), limiting to min 0.0");
	                deployment = 0.0;
	            }
	            controlSurfaces.setAirbrakeDeployment(deployment);
	            System.out.println("Airbrake set to: " + (deployment * 100) + "%");
	            break;

	        default:
	            System.out.println("Unknown control surface: " + surface);
	    }
	}
	public void thrust(String property, int value ) {
		
		IFADEC fadec = OSGiUtils.getService(context, IFADEC.class);
		if (fadec == null) {
			System.out.println("FADEC not available.");
			return;
		}
		
		if (property.equalsIgnoreCase("set")) {
			try {
				int throttleValue = value; // Assuming value is already a int
				if(throttleValue < 0 || throttleValue > 100) {
					System.out.println("Throttle value must be between 0 and 100.");
					return;
				}
				fadec.setTHRUSTPercentage(throttleValue);
				System.out.println("Throttle set to: " + throttleValue);
			} catch (NumberFormatException e) {
				System.out.println("Invalid throttle value: " + value);
			}
		} else if (property.equalsIgnoreCase("get")) {
			System.out.println("Current Throttle: " + fadec.getCurrentThrust());
		} else {
			System.out.println("Unknown property: " + property);
		}
		
	}
	
	
	public void timeStep(double value) {
		
		if (value < 0.1 || value > 10) {
			System.out.println("Time step value must be between 0.1 and 10 seconds.");
			return;
		}
		
		PlaneSimulationElement.setTimeStepSeconds(value);
		System.out.println("Time step set to: " + value + " seconds");
		
	}
	public void terrainAltitude(double value) {
		IRadioAltimeterSensor radioAltimeter = OSGiUtils.getService(context, IRadioAltimeterSensor.class);
		IAltitudeSensor altitudeSensor = OSGiUtils.getService(context, IAltitudeSensor.class);
		INavigationSystem gnss = OSGiUtils.getService(context, INavigationSystem.class);
		if (radioAltimeter == null) {
			System.out.println("Radio Altimeter not available.");
			return;
		}
			try {
				double groundaltitudeValue = value; // Assuming value is already a double
				if(groundaltitudeValue < 0 || groundaltitudeValue > RadioAltimeterSensor.MAX_REALGROUND_ALTITUDE ) {
					System.out.println("Altitude value must be between 0 and " + RadioAltimeterSensor.MAX_REALGROUND_ALTITUDE);
					return;

				}
					
				if(gnss.getCurrentFlyghtStage() == EFlyingStages.DESCENT || gnss.getCurrentFlyghtStage() == EFlyingStages.LANDING) {
					System.out.println("Terrain Altitude Cant be set on Landing or Descent stages " + RadioAltimeterSensor.MAX_REALGROUND_ALTITUDE);
					return;
				}
				if(altitudeSensor.getAltitude() < groundaltitudeValue && !(gnss.getCurrentDistance() == 0)) {
					System.out.println("Altitude value must be less than the current altitude: " + altitudeSensor.getAltitude());
					return;
				}
				
				if(gnss.getCurrentDistance() == 0) {
					altitudeSensor.setAltitude(groundaltitudeValue);
				}
				radioAltimeter.setGroundDistance(RadioAltimeterSensor.calculateGroundDistance(altitudeSensor.getAltitude(), groundaltitudeValue ));
				radioAltimeter.setRealGroundAltitude(groundaltitudeValue);
				System.out.println("Ground Altitude set to: " + groundaltitudeValue);
			} catch (NumberFormatException e) {
				System.out.println("Invalid ground altitude value: " + value);
			}
		
	}
	public void setEngineFailure(boolean value) {
	    IFADEC fadec = OSGiUtils.getService(context, IFADEC.class);
	    if (fadec == null) {
	        System.out.println("FADEC not available.");
	        return;
	    }

	    boolean currentFailureState = fadec.isFailure();

	    if (currentFailureState == value) {
	        System.out.println(value 
	            ? "Engine is already in failure state." 
	            : "Engine is already in normal state.");
	        return;
	    }

	    if (!value && currentFailureState) {
	        System.out.println("Cannot revert engine to normal state once it has failed.");
	        return;
	    }

	    fadec.setFailure(value);
	    System.out.println("Engine state set to: " + (value ? "Failure" : "Normal"));
	}
	public void changeFlyingAssistance(boolean value) {
		IFlyingService flyingService = OSGiUtils.getService(context, IFlyingService.class);
		if(flyingService == null) {
			System.out.println("Flying Service not available.");
			return;
		}
		if(flyingService instanceof IL0_ManualNavigation) {
			System.out.println("Flying Service is already in manual navigation.");
		} else if(flyingService instanceof IL1_FlyingService) {
			((IL1_FlyingService) flyingService).setStabilityModeActive(value);
			
		} else if(flyingService instanceof IL2_FlyingService) {
			((IL2_FlyingService) flyingService).setStabilityModeActive(value);
		} else if(flyingService instanceof IL3_FlyingService) {
			((IL3_FlyingService) flyingService).setStabilityModeActive(value);
		}
		System.out.println("Flying Assistance changed to: " + (value ? "ON" : "OFF"));
		
	}
	public void flyingStage(String stage) {
		  INavigationSystem gnss = OSGiUtils.getService(context, INavigationSystem.class,
			        "(" + IIdentifiable.ID + "=GNSS)");
			    if (gnss == null) {
			        gnss = OSGiUtils.getService(context, INavigationSystem.class,
			            "(" + IIdentifiable.ID + "=RadioNavigationSystem)");
			    }
	    System.out.println("Setting flying stage to: " + gnss.getClass().getName());
		//ILandingSystem landingSystem = OSGiUtils.getService(context, ILandingSystem.class);
		IAltitudeSensor altitudeSensor = OSGiUtils.getService(context, IAltitudeSensor.class);
		ISpeedSensor speedSensor = OSGiUtils.getService(context, ISpeedSensor.class);
		IFADEC fadec = OSGiUtils.getService(context, IFADEC.class);
		IRadioAltimeterSensor radioAltimeter = OSGiUtils.getService(context, IRadioAltimeterSensor.class);
		if(gnss == null || altitudeSensor == null || speedSensor == null || fadec == null) {
			System.out.println("A sensor is not available.");
			return;
		}

		if(gnss.getCurrentFlyghtStage() != EFlyingStages.TAKEOFF) {
			System.out.println("Destination can only be set during TAKEOFF stage.");
			return;
		}
		System.out.println("Setting flying stage to: " + stage);
		System.out.println("Resetting real ground altitude to 0");
		if(stage.toLowerCase().equals("takeoff")) {
			gnss.setCurrentDistance(0);
			altitudeSensor.setAltitude(0); // Set initial altitudede for Castellon
			radioAltimeter.setGroundDistance(0); // Set initial ground distance for Castellon
			radioAltimeter.setRealGroundAltitude(0); // Set initial ground altitude for Castellon
			speedSensor.setSpeedTAS(0); // Set initial ground speed for Castellon
			speedSensor.setSpeedGS(0); // Set initial ground speed for Castellon
			fadec.setTHRUSTPercentage(0); // Set initial thrust for Castellon
			gnss.setCurrentFlyghtStage(EFlyingStages.TAKEOFF);
		} else if(stage.toLowerCase().equals("landing")) {
			gnss.setCurrentDistance(495000);
			altitudeSensor.setAltitude(300); // Set initial altitudede for Castellon´
			radioAltimeter.setGroundDistance(300); // Set initial ground altitude for Castellon
			radioAltimeter.setRealGroundAltitude(0); // Set initial ground altitude for Castellon

			speedSensor.setSpeedTAS(80); // Set initial ground speed for Castellon
			speedSensor.setSpeedGS(80); // Set initial ground speed for Castellon
			fadec.setTHRUSTPercentage(20); // Set initial thrust for Castellon
			gnss.setCurrentFlyghtStage(EFlyingStages.LANDING);
		} else if(stage.toLowerCase().equals("cruise")) {
			gnss.setCurrentDistance(206000);
			altitudeSensor.setAltitude(10000); // Set initial altitude for cruise
			radioAltimeter.setGroundDistance(2500); // Set initial ground altitude for Castellon
			radioAltimeter.setRealGroundAltitude(0); // Set initial ground altitude for Castellon
			speedSensor.setSpeedTAS(230); // Set initial ground speed for cruise
			speedSensor.setSpeedGS(230); // Set initial ground speed for cruise
			fadec.setTHRUSTPercentage(70); // Set initial thrust for cruise
			gnss.setCurrentFlyghtStage(EFlyingStages.CRUISE);
		} else if(stage.toLowerCase().equals("climb")) {
			gnss.setCurrentDistance(2500);
			altitudeSensor.setAltitude(60); // Set initial altitude for approach
			radioAltimeter.setGroundDistance(60); // Set initial ground altitude for Castellon
			radioAltimeter.setRealGroundAltitude(0); // Set initial ground altitude for Castellon
			speedSensor.setSpeedTAS(100); // Set initial ground speed for approach
			speedSensor.setSpeedGS(100); // Set initial ground speed for approach
			fadec.setTHRUSTPercentage(90); // Set initial thrust for approach
			gnss.setCurrentFlyghtStage(EFlyingStages.CLIMB);
		} else if(stage.toLowerCase().equals("descent")) {
			gnss.setCurrentDistance(290000);
			altitudeSensor.setAltitude(10000); // Set initial altitude for approach
			radioAltimeter.setGroundDistance(2500); // Set initial ground altitude for Castellon
			radioAltimeter.setRealGroundAltitude(0); // Set initial ground altitude for Castellon
			speedSensor.setSpeedTAS(170); // Set initial ground speed for approach
			speedSensor.setSpeedGS(170); // Set initial ground speed for approach
			fadec.setTHRUSTPercentage(50); // Set initial thrust for approach
			gnss.setCurrentFlyghtStage(EFlyingStages.DESCENT);
		}else {
			System.out.println("Unknown flying stage: " + stage);
			return;
		}
	}
	
	/*public void wind(String property, double value) {
		IWeatherSensor weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
		if (weatherSensor == null) {
			System.out.println("Weather Sensor not available.");
			return;
		}
		
		if (property.equalsIgnoreCase("speed")) {
			try {
				double windSpeed = value; // Assuming value is already a double
				weatherSensor.setWindSpeed(windSpeed);
				System.out.println("Wind speed set to: " + windSpeed);
			} catch (NumberFormatException e) {
				System.out.println("Invalid wind speed value: " + value);
			}
		} else if (property.equalsIgnoreCase("direction")) {
				double windDirection = value; // Assuming value is already a double
				weatherSensor.setWindDirection(windDirection);
		} else {
			System.out.println("Unknown property: " + property);
		}
		
	}*/
	public void groundTemperature(String property, double value) {
		IWeatherSensor weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
		IAltitudeSensor altitudeSensor = OSGiUtils.getService(context, IAltitudeSensor.class);
		if (weatherSensor == null && altitudeSensor == null) {
			System.out.println("Weather Sensor not available.");
			return;
		}
		
		if (property.equalsIgnoreCase("set")) {
			try {
				double groundTempValue = value; // Assuming value is already a double
				weatherSensor.setGroundTemperature(groundTempValue);
				weatherSensor.setTemperature(weatherSensor.calculateTemp(altitudeSensor.getAltitude()));
				System.out.println("Ground Temperature set to: " + groundTempValue);
			} catch (NumberFormatException e) {
				System.out.println("Invalid ground temperature value: " + value);
			}
		} else if (property.equalsIgnoreCase("get")) {
			System.out.println("Current Ground Temperature: " + weatherSensor.getGroundTemperature());
		} else {
			System.out.println("Unknown property: " + property);
		}
		
	}
	public void climate(String property, String value) {
		IWeatherSensor weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
		if (weatherSensor == null) {
			System.out.println("Weather Sensor not available.");
			return;
		}
		if(value.toLowerCase().equals("clear")) {
			weatherSensor.setActualClimate(EClimate.CLEAR);
			weatherSensor.setWindSpeed(10.0);
		} else if(value.toLowerCase().equals("stormy")) {
			weatherSensor.setActualClimate(EClimate.STORMY);
			weatherSensor.setWindSpeed(70.0);

		} else {
			System.out.println("Unknown climate value: " + value);
			return;
		}
		
	
		
	}
	public void fuelLevel(String property, String value) {
		IFuelSensor fuelSensor = OSGiUtils.getService(context, IFuelSensor.class,
			    "(" + IIdentifiable.ID + "=CapacitiveFuelSensor)");

			if (fuelSensor == null) {
			    fuelSensor = OSGiUtils.getService(context, IFuelSensor.class,
			        "(" + IIdentifiable.ID + "=FloatSensor)");
			}
			
		INavigationSystem gnss = OSGiUtils.getService(context, INavigationSystem.class,
			        "(" + IIdentifiable.ID + "=GNSS)");
			    if (gnss == null) {
			        gnss = OSGiUtils.getService(context, INavigationSystem.class,
			            "(" + IIdentifiable.ID + "=RadioNavigationSystem)");
			    }
		
		if (fuelSensor == null && gnss == null) {
			System.out.println("Fuel Sensor or Navigation System not available.");
			return;
		}
		if(gnss.getCurrentFlyghtStage() != EFlyingStages.TAKEOFF) {
			System.out.println("Fuel level can only be set during TAKEOFF stage.");
			return;
		}
		if (property.equalsIgnoreCase("kg")) {
			try {
				double fuelLevel = Double.parseDouble(value); // Assuming value is a string that can be parsed to double
				if(fuelLevel < 0 || fuelLevel > FuelSensor.MAX_FUEL_KG) {
					System.out.println("Fuel level must be between 0 + " + FuelSensor.MAX_FUEL_KG + " kg.");
					return;
				}
				fuelSensor.setFuelLevel(fuelLevel);
				System.out.println("Fuel level set to: " + fuelLevel + " kg");
			} catch (NumberFormatException e) {
				System.out.println("Invalid fuel level value: " + value);
			}
		} else if (property.equalsIgnoreCase("percentage")) {
			try {
				double fuelPercentage = Double.parseDouble(value); // Assuming value is a string that can be parsed to double
				if(fuelPercentage < 0 || fuelPercentage > 100) {
					System.out.println("Fuel percentage must be between 0 and 100.");
					return;
				}
				fuelSensor.setFuelPercentage(fuelPercentage);
				System.out.println("Fuel percentage set to: " + fuelPercentage + "%");
			} catch (NumberFormatException e) {
				System.out.println("Invalid fuel percentage value: " + value);
			}
		} else {
			System.out.println("Unknown property: " + property);
		}
	}
	
	public void setCloud(boolean Value) {
		IWeatherSensor weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
		if (weatherSensor == null) {
			System.out.println("Weather Sensor not available.");
			return;
		}
		weatherSensor.setInCloud(Value);
		System.out.println("In Cloud set to: " + Value);
	}
	public void setCooling(boolean Value) {
		IEGTSensor egtSensor = OSGiUtils.getService(context, IEGTSensor.class);
		if (egtSensor == null) {
			System.out.println("EGT Sensor not available.");
			return;
		}
		egtSensor.setCoolingEnabled(Value);
		System.out.println("Cooling Enabled set to: " + Value);
	}
	public void setHeating(boolean Value) {
		IEGTSensor egtSensor = OSGiUtils.getService(context, IEGTSensor.class);
		if (egtSensor == null) {
			System.out.println("EGT Sensor not available.");
			return;
		}
		egtSensor.setHeatingEnabled(Value);
		System.out.println("Heating Enabled set to: " + Value);
	}
	public void objectDetected(boolean Value) {
		IProximitySensor proximitySensor = OSGiUtils.getService(context, IProximitySensor.class,
			    "(" + IIdentifiable.ID + "=LIDARSensor)");

			if (proximitySensor == null) {
			    proximitySensor = OSGiUtils.getService(context, IProximitySensor.class,
			        "(" + IIdentifiable.ID + "=FMCWRadarSensor)");
			}
		IRadioAltimeterSensor altitudeSensor = OSGiUtils.getService(context, IRadioAltimeterSensor.class);
		if (proximitySensor == null || altitudeSensor == null) {
			System.out.println("Proximity sensor or altitudeSensor not available.");
			return;
		}
		double currentAltitude = altitudeSensor.getGroundDistance();
		if (Value && currentAltitude > 150.0) {  // Altitude in meters (~500 feet)
		    System.out.println("Object detection ignored: altitude too high (" + currentAltitude + " m).");
		    return;
		}
		proximitySensor.setObjectDetected(Value);
		System.out.println("The value of the proximity objects has changed:" + Value);
	}
	public void flying(String function) {
	    if (function.equalsIgnoreCase("L0")) {

	        // 1. Crear las dependencias (sensor y mecanismos de interacción)
	        INotificationService notificationService = new NotificationService(context, INotificationService.class.getName());
	        IInteractionMechanism stallWarning = new StallWarning(context, StallWarning.class.getName());
	        IAOASensor aoaSensor = new AOASensor(context, "aoaSensor");

	        Dictionary<String, Object> sensorProps = new Hashtable<>();
	        sensorProps.put("aoaSensor", "aoaSensor");

	        // 2. Registrar los servicios básicos
	        notificationService.addInteractionMechanism(StallWarning.class.getName());

	        context.registerService(INotificationService.class.getName(), notificationService, null);
	        context.registerService(StallWarning.class.getName(), stallWarning, null);
	        context.registerService(IAOASensor.class.getName(), aoaSensor, sensorProps);

	        // 3. Crear el ARC de navegación (no arranca aún)
	        String navId = "L0_MANUAL";
	        L0_ManualNavigationARC navARC = new L0_ManualNavigationARC(context, navId);

	        // 4. Obtener el servicio de navegación y setear dependencias ANTES de usar
	    //    IL0_ManualNavigation navService = navARC.getTheL0_ManualNavigationService();
	       // navService.setAOASensor(aoaSensor);
	        //navService.setNotificationService(notificationService);

	        // 5. Registrar el servicio de vuelo (ahora completamente inicializado)
	       // context.registerService(IFlyingService.class.getName(), navService, null);
	       // navService.startFlight();
	        System.out.println("L0_ManualNavigation y AOASensor iniciados, enlazados y registrados correctamente.");

	    } else {
	        System.out.println("Flying function is not implemented in this version.");
	    }
	}


	public void next() {
		IManualSimulatorStepsManager manager = OSGiUtils.getService(context, IManualSimulatorStepsManager.class);
		if ( manager != null )
			manager.next();
	}
	
	public void n() {
		this.next();
	}

}
