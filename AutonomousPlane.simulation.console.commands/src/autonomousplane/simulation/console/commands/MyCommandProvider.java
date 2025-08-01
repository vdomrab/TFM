package autonomousplane.simulation.console.commands;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IL0_ManualNavigation;
import autonomousplane.devices.interfaces.*;
import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.devices.AOASensor;
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
		INavigationSystem gnss = OSGiUtils.getService(context, INavigationSystem.class);
		IWeatherSensor weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
		ILandingSystem landingSystem = OSGiUtils.getService(context, ILandingSystem.class);
		IFuelSensor fuelSensor = OSGiUtils.getService(context, IFuelSensor.class);
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
			System.out.println(String.format("|     Speed (GS): %s m/s", speedSensor.getSpeedGS()));
			System.out.println(String.format("|     Speed (TAS): %s m/s", speedSensor.getSpeedTAS()));
			System.out.println(String.format("|     Thrust: %s %%", fadec.getCurrentThrust()));
			System.out.println(String.format("|     Acceleration(TAS): %s m/s²", speedSensor.getSpeedIncreaseTAS()));
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");


		}
		if(fuelSensor != null && egtSensor != null) {
			System.out.println("|                  Engine INFO");
			System.out.println(" - - - - - - - - - - - - - - - - - - - - - - - -\n");
			System.out.println(String.format("|     Fuel Level: %s kg", fuelSensor.getFuelLevel()));
			System.out.println(String.format("|     Fuel Level: %s %%", fuelSensor.getFuelPercentage()));
			System.out.println(String.format("|     Fuel Consumption Rate: %s kg", fuelSensor.getFuelConsumptionRate()));
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
			System.out.println(String.format("|     Wind Speed: %s m/s", weatherSensor.getWindSpeed()));	
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
			/*
			IKnowledgeProperty kp_myProp = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("my-prop");
			
			String myProp = "UNKNOWN";
			if ( kp_myProp != null && kp_myProp.getValue() != null )
				myProp = kp_myProp.getValue().toString();
					
			System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");
			System.out.println("*  KNOWLEDGE");
			System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");
			System.out.println(String.format("*   my-prop: %s", myProp));
			// ...
			System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");*/

		    // Obtener las propiedades de conocimiento
		    /*IKnowledgeProperty kp_roadType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road_type");
		    IKnowledgeProperty kp_autonomusType = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("autonomus_type");
		    IKnowledgeProperty kp_roadStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("road_status");
		    IKnowledgeProperty kp_componentStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("component_status");
		    IKnowledgeProperty kp_driverStatus = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("face_status");
		    IKnowledgeProperty kp_handsOnWheel = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("hands_on_wheel");
		    IKnowledgeProperty kp_seatedOnPilot = BasicMAPEKLiteLoopHelper.getKnowledgeProperty("seated_on_pilot");
		    
		    // Inicializar variables para almacenar los valores
		    String roadType = "UNKNOWN";
		    String autonomusType = "UNKNOWN";
		    String roadStatus = "UNKNOWN";
		    String driverStatus = "UNKNOWN";
		    Boolean handsOnWheel = false;
		    Boolean seatedOnPilot = false;
		    int componentStatus = 0;

		    // Obtener los valores de las propiedades, si están disponibles
		    if (kp_roadType != null && kp_roadType.getValue() != null) {
		        roadType = kp_roadType.getValue().toString();
		    }
		    
		    if (kp_driverStatus != null && kp_driverStatus.getValue() != null) {
		        driverStatus = kp_driverStatus.getValue().toString();
		    }

		    if (kp_seatedOnPilot != null && kp_seatedOnPilot.getValue() != null) {
		    	seatedOnPilot = (Boolean) kp_seatedOnPilot.getValue();
		    }
		    if (kp_handsOnWheel != null && kp_handsOnWheel.getValue() != null) {
		        handsOnWheel = (Boolean) kp_handsOnWheel.getValue();
		    }
		    
		    if (kp_autonomusType != null && kp_autonomusType.getValue() != null) {
		        autonomusType = kp_autonomusType.getValue().toString();
		    }
		    if (kp_roadStatus != null && kp_roadStatus.getValue() != null) {
		        roadStatus = kp_roadStatus.getValue().toString();
		    }
		    if (kp_componentStatus != null && kp_componentStatus.getValue() != null) {
		        Set<String> componentStatusList = ((Set<String>) kp_componentStatus.getValue());
		        componentStatus = componentStatusList.size();
		    }
	        Set<String> componentStatusList = ((Set<String>) kp_componentStatus.getValue());

		    // Imprimir los valores de las propiedades de conocimiento
		    System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");
		    System.out.println("*  KNOWLEDGE");
		    System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");
		    System.out.println(String.format("*   road_type: %s", roadType));
		    System.out.println(String.format("*   autonomus_type: %s", autonomusType));
		    System.out.println(String.format("*   road_status: %s", roadStatus));
		    System.out.println(String.format("*   component_status: %s", componentStatusList));
		    System.out.println(String.format("*   component_status_count: %s", componentStatus));
		    System.out.println(String.format("*   Driver's Face: %s", driverStatus));
		    System.out.println(String.format("*   HandsOnWheel: %s", handsOnWheel));
		    System.out.println(String.format("*   SeatedOnPilot: %s", seatedOnPilot));

		    System.out.println("* * * * * * * * * * * * * * * * * * * * * * * *");*/
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
	
	public void controlSurface(String property, String surface, double value) {
		IControlSurfaces controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
		if (controlSurfaces == null) {
			System.out.println("Control Surfaces not available.");
			return;
		}
		
		switch (surface.toLowerCase()) {
			case "aileron":
				if (property.equalsIgnoreCase("set")) {
					controlSurfaces.setAileronDeflection(value);
					System.out.println("Aileron set to: " + value);
				} else if (property.equalsIgnoreCase("get")) {
					System.out.println("Current Aileron deflection: " + controlSurfaces.getAileronDeflection());
				} else {
					System.out.println("Unknown property for Aileron: " + property);
				}
				break;
			case "elevator":
				if (property.equalsIgnoreCase("set")) {
					controlSurfaces.setElevatorDeflection(value);
					System.out.println("Elevator set to: " + value);
				} else if (property.equalsIgnoreCase("get")) {
					System.out.println("Current Elevator deflection: " + controlSurfaces.getElevatorDeflection());
				} else {
					System.out.println("Unknown property for Elevator: " + property);
				}
				break;
			case "rudder":
				if (property.equalsIgnoreCase("set")) {
					controlSurfaces.setRudderDeflection(value);
					System.out.println("Rudder set to: " + value);
				} else if (property.equalsIgnoreCase("get")) {
					System.out.println("Current Rudder deflection: " + controlSurfaces.getRudderDeflection());
				} else {
					System.out.println("Unknown property for Rudder: " + property);
				}
				break;
			case "airbrake":
				if (property.equalsIgnoreCase("set")) {
					controlSurfaces.setAirbrakeDeployment(value/100);
					System.out.println("Airbreak set to: " + value);
				} else if (property.equalsIgnoreCase("get")) {
					System.out.println("Current Rudder deflection: " + controlSurfaces.getRudderDeflection());
				} else {
					System.out.println("Unknown property for Rudder: " + property);
				}
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
	
	public void speed(String property, double value) {
		ISpeedSensor speedSensor = OSGiUtils.getService(context, ISpeedSensor.class);
		IFADEC fadec = OSGiUtils.getService(context, IFADEC.class);
		if (speedSensor == null && fadec == null) {
			System.out.println("Speed Sensor or FADEC not available.");
			return;
		}
		
		if (property.equalsIgnoreCase("set")) {
			try {
				double speedValue = value; // Assuming value is already a double
				System.out.println("Speed set to: " + speedValue);
			} catch (NumberFormatException e) {
				System.out.println("Invalid speed value: " + value);
			}
		} else if (property.equalsIgnoreCase("get")) {
		} else {
			System.out.println("Unknown property: " + property);
		}
		
	}
	public void timeStep(double value) {
		
		if (value < 0.1 && value > 10) {
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
				if(groundaltitudeValue < 0 || groundaltitudeValue > RadioAltimeterSensor.MAX_REALGROUND_ALTITUDE) {
					System.out.println("Altitude value must be between 0 and " + RadioAltimeterSensor.MAX_REALGROUND_ALTITUDE);
					return;
				}
				if(altitudeSensor.getAltitude() < groundaltitudeValue && gnss.getCurrentFlyghtStage() != EFlyingStages.TAKEOFF) {
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
	public void ascend(String value) {
		 	double thurst;
		    double elevatorDeflection;

		    IFADEC thrustSensor = OSGiUtils.getService(context, IFADEC.class);
		    ISpeedSensor speedSensor = OSGiUtils.getService(context, ISpeedSensor.class);
		    IAttitudeSensor AHRSS = OSGiUtils.getService(context, IAttitudeSensor.class);
		    IControlSurfaces controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
		    if (thrustSensor == null || speedSensor == null || AHRSS == null || controlSurfaces == null) {
		        System.out.println("Sensors or control surfaces not available.");
		        return;
		    }

		    switch (value.toLowerCase()) {
		        case "slow":
		        	thurst = 50;
		            elevatorDeflection = -5.0; // Small negative deflection for gentle descent
		            break;
		        case "middle":
		        	thurst = 60;
		            elevatorDeflection = -10.0; // Moderate negative deflection
		            break;
		        case "fast":
		        	thurst = 90;
		            elevatorDeflection = -5; // Larger negative deflection for steep descent
		            break;
		        default:
		            throw new IllegalArgumentException("Tipo de descenso no reconocido: " + value);
		    }
		  
		    thrustSensor.setTHRUSTPercentage(thurst);
		    controlSurfaces.setElevatorDeflection(elevatorDeflection); // Use control surface
		}
	
	public void descend(String value) {
	    double thurst;
	    double elevatorDeflection;

	    IFADEC thrustSensor = OSGiUtils.getService(context, IFADEC.class);
	    ISpeedSensor speedSensor = OSGiUtils.getService(context, ISpeedSensor.class);
	    IAttitudeSensor AHRSS = OSGiUtils.getService(context, IAttitudeSensor.class);
	    IControlSurfaces controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
	    if (thrustSensor == null || speedSensor == null || AHRSS == null || controlSurfaces == null) {
	        System.out.println("Sensors or control surfaces not available.");
	        return;
	    }

	    switch (value.toLowerCase()) {
	        case "slow":
	        	thurst = 50;
	            elevatorDeflection = 5.0; // Small negative deflection for gentle descent
	            break;
	        case "middle":
	        	thurst = 60;
	            elevatorDeflection = 10.0; // Moderate negative deflection
	            break;
	        case "fast":
	        	thurst = 70;
	            elevatorDeflection = 15.0; // Larger negative deflection for steep descent
	            break;
	        default:
	            throw new IllegalArgumentException("Tipo de descenso no reconocido: " + value);
	    }

	    thrustSensor.setTHRUSTPercentage(thurst);
	    controlSurfaces.setElevatorDeflection(elevatorDeflection); // Use control surface
	}
	public void flyingStage(String stage) {
		INavigationSystem gnss = OSGiUtils.getService(context, INavigationSystem.class);
		ILandingSystem landingSystem = OSGiUtils.getService(context, ILandingSystem.class);
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
	
	public void wind(String property, double value) {
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
		
	}
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
		IFuelSensor fuelSensor = OSGiUtils.getService(context, IFuelSensor.class);
		INavigationSystem gnss = OSGiUtils.getService(context, INavigationSystem.class);
		
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
		IProximitySensor proximitySensor = OSGiUtils.getService(context, IProximitySensor.class);
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


	
	/*public void driver(String property, String s) {
		IHumanSensors sensor = OSGiUtils.getService(context, IHumanSensors.class);
		if ( sensor == null )
			return;
		if ( property.equalsIgnoreCase("face") ) {
			if ( s.equalsIgnoreCase("looking-forward") || s.equalsIgnoreCase("l") ) {
				sensor.setFaceStatus(EFaceStatus.LOOKING_FORWARD);
			}
			else if ( s.equalsIgnoreCase("distracted") || s.equalsIgnoreCase("d") ) {
				sensor.setFaceStatus(EFaceStatus.DISTRACTED);
			}
			else if ( s.equalsIgnoreCase("sleeping") || s.equalsIgnoreCase("s") ) {
				sensor.setFaceStatus(EFaceStatus.SLEEPING);
			}
		} else if ( property.equalsIgnoreCase("hands") ) {
			if ( s.equalsIgnoreCase("on-wheel") ) {
				sensor.setTheHandsOnTheSteeringWheel(true);
			} else if ( s.equalsIgnoreCase("off-wheel") ) {
				sensor.setTheHandsOnTheSteeringWheel(false);
			}
		}
		System.out.println("Driver property: " + property + " value: " + sensor.getFaceStatus().toString());

	}
		
	public void road(String property, String s) {
		IRoadSensor rs = OSGiUtils.getService(context, IRoadSensor.class);
		if ( rs == null )
			return;
		if ( property.equalsIgnoreCase("type") ) {
			if ( s.equalsIgnoreCase("std") || s.equalsIgnoreCase("s") )
				rs.setRoadType(ERoadType.STD_ROAD);
			else if ( s.equalsIgnoreCase("city") || s.equalsIgnoreCase("c") )
				rs.setRoadType(ERoadType.CITY);
			else if ( s.equalsIgnoreCase("highway") || s.equalsIgnoreCase("h") )
				rs.setRoadType(ERoadType.HIGHWAY);
			else if ( s.equalsIgnoreCase("off-road") || s.equalsIgnoreCase("o") )
				rs.setRoadType(ERoadType.OFF_ROAD);
			
		} else if ( property.equalsIgnoreCase("status") ) {
			if ( s.equalsIgnoreCase("fluid") || s.equalsIgnoreCase("f") ) {
				rs.setRoadStatus(ERoadStatus.FLUID);
			} else if ( s.equalsIgnoreCase("jam") || s.equalsIgnoreCase("j") ) {
				rs.setRoadStatus(ERoadStatus.JAM);
			} else if ( s.equalsIgnoreCase("collapsed") || s.equalsIgnoreCase("c") ) {
				rs.setRoadStatus(ERoadStatus.COLLAPSED);
			}
		}
	}

	public void seat(String s, boolean value) {
		IHumanSensors sensor = OSGiUtils.getService(context, IHumanSensors.class);
		if ( sensor == null )
			return;
		if ( s.equalsIgnoreCase("driver") )
			sensor.setDriverSeatOccupancy(value);
		else if ( s.equalsIgnoreCase("copilot") )
			sensor.setCopilotSeatOccupancy(value);
	}
	
	public void driving(String function) {
		
		System.out.println("Function disabled in this Adaptive version!");
		return;
		
		
	}

	public void engine(String s, int rpm) {
		IEngine engine = OSGiUtils.getService(context, IEngine.class);
		if ( engine == null )
			return;
		if ( s.equalsIgnoreCase("rpm") )
			engine.setRPM(rpm);
		else if ( s.equalsIgnoreCase("accelerate") )
			engine.accelerate(rpm);
		else if ( s.equalsIgnoreCase("decelerate") )
			engine.decelerate(rpm);
	}

	public void steering(String s, int angle) {
		ISteering steering = OSGiUtils.getService(context, ISteering.class);
		if ( steering == null )
			return;
		else if ( s.equalsIgnoreCase("right") )
			steering.rotateRight(angle);
		else if ( s.equalsIgnoreCase("left") )
			steering.rotateLeft(angle);
	}
	
	public void line(String line, boolean value) {
		// line = Left or Right (case insensitive)
		String sensorId = null;
		if ( line.equalsIgnoreCase("left") )
			sensorId = "LeftLineSensor";
		else if ( line.equalsIgnoreCase("right") )
			sensorId = "RightLineSensor";
		
		ILineSensor sensor = OSGiUtils.getService(context, ILineSensor.class, String.format("(id=%s)", sensorId));
		if ( sensor != null )
			sensor.setLineDetected(value);
	}
	
	public void distance(String l, int distance) {
		// l = Front | Rear | Left | Right (case insensitive)
		String sensorId = null;
		if ( l.equalsIgnoreCase("front") )
			sensorId = "FrontDistanceSensor";
		else if ( l.equalsIgnoreCase("rear") )
			sensorId = "RearDistanceSensor";
		else if ( l.equalsIgnoreCase("right") )
			sensorId = "RightDistanceSensor";
		else if ( l.equalsIgnoreCase("left") )
			sensorId = "LeftDistanceSensor";

		IDistanceSensor sensor = OSGiUtils.getService(context, IDistanceSensor.class, String.format("(id=%s)", sensorId));
		if ( sensor != null )
			sensor.setDistance(distance);
	}

	public void lidar(String l, int distance) {
		// l = Front | Rear | Left | Right (case insensitive)
		String sensorId = null;
		if ( l.equalsIgnoreCase("front") )
			sensorId = "LIDAR-FrontDistanceSensor";
		else if ( l.equalsIgnoreCase("rear") )
			sensorId = "LIDAR-RearDistanceSensor";
		else if ( l.equalsIgnoreCase("right") )
			sensorId = "LIDAR-RightDistanceSensor";
		else if ( l.equalsIgnoreCase("left") )
			sensorId = "LIDAR-LeftDistanceSensor";

		IDistanceSensor sensor = OSGiUtils.getService(context, IDistanceSensor.class, String.format("(id=%s)", sensorId));
		if ( sensor != null )
			sensor.setDistance(distance);
	}*/
	
	public void next() {
		IManualSimulatorStepsManager manager = OSGiUtils.getService(context, IManualSimulatorStepsManager.class);
		if ( manager != null )
			manager.next();
	}
	
	public void n() {
		this.next();
	}

}
