package autonomousplane.mapeklite;



import java.util.List;

import javax.naming.Binding;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.autopilotARC.FallbackPlanARC;
import autonomousplane.infraestructure.autopilotARC.L0_FlyingServiceARC;
import autonomousplane.infraestructure.autopilotARC.L1_FlyingServiceARC;
import autonomousplane.infraestructure.autopilotARC.L2_FlyingServiceARC;
import autonomousplane.infraestructure.autopilotARC.L3_FlyingServiceARC;
import autonomousplane.infraestructure.devices.ARC.AHRSSensorARC;
import autonomousplane.infraestructure.devices.ARC.AOASensorARC;
import autonomousplane.infraestructure.devices.ARC.AltitudeSensorARC;
import autonomousplane.infraestructure.devices.ARC.ControlSurfacesARC;
import autonomousplane.infraestructure.devices.ARC.EGTSensorARC;
import autonomousplane.infraestructure.devices.ARC.ETLARC;
import autonomousplane.infraestructure.devices.ARC.FADECARC;
import autonomousplane.infraestructure.devices.ARC.FuelSensorARC;
import autonomousplane.infraestructure.devices.ARC.LandingSystemARC;
import autonomousplane.infraestructure.devices.ARC.NavigationSystemARC;
import autonomousplane.infraestructure.devices.ARC.ProximitySensorARC;
import autonomousplane.infraestructure.devices.ARC.RadioAltimeterSensorARC;
import autonomousplane.infraestructure.devices.ARC.SpeedSensorARC;
import autonomousplane.infraestructure.devices.ARC.WeatherSensorARC;
import autonomousplane.infraestructure.interaction.ARC.FrozenWarningARC;
import autonomousplane.infraestructure.interaction.ARC.GeneralWarningARC;
import autonomousplane.infraestructure.interaction.ARC.NotificationServiceARC;
import autonomousplane.infraestructure.interaction.ARC.OverheatWarningARC;
import autonomousplane.infraestructure.interaction.ARC.StallWarningARC;
import autonomousplane.infraestructure.interaction.ARC.TAWSARC;
import autonomousplane.mapeklite.adaptationrules.ADS_EngineFailRule;
import autonomousplane.mapeklite.adaptationrules.ADS_L1;
import autonomousplane.mapeklite.adaptationrules.ADS_LowFuel;
import autonomousplane.mapeklite.adaptationrules.ADS_LowFuelOFF;
import autonomousplane.mapeklite.adaptationrules.ADS_RoughWeather;
import autonomousplane.mapeklite.adaptationrules.ADS_RoughWeatherOFF;
import autonomousplane.mapeklite.adaptationrules.ADS_TAWSWarning;
import autonomousplane.mapeklite.adaptationrules.ADS_ThermalFallbackPlan;
import autonomousplane.mapeklite.monitors.AOAValueMonitor;
import autonomousplane.mapeklite.monitors.AutonomousTypeMonitor;
import autonomousplane.mapeklite.monitors.ComponentStatusMonitor;
import autonomousplane.mapeklite.monitors.DistanceMonitor;
import autonomousplane.mapeklite.monitors.EngineMonitor;
import autonomousplane.mapeklite.monitors.FallbackPlanTypeMonitor;
import autonomousplane.mapeklite.monitors.VerticalSpeedMonitor;
import autonomousplane.mapeklite.probes.Aoaprobe;
import autonomousplane.mapeklite.probes.AutonomousProbe;
import autonomousplane.mapeklite.probes.ComponentStatusProbe;
import autonomousplane.mapeklite.probes.FallbackPlanProbe;
import autopilot.stallrecoveryfallbackplan.StallRecoveryFallbackPlanARC;
import autopilot.thermalfallbackplan.ThermalFallbackPlanARC;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.artifacts.interfaces.IKnowledgeProperty;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.BasicMAPEKLiteLoopHelper;
import es.upv.pros.tatami.adaptation.mapek.lite.helpers.SystemConfigurationHelper;
import es.upv.pros.tatami.osgi.utils.interfaces.ITimeStamped;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		System.out.println("Starting Autonomous Plane MAPE-K Lite adaptation starter...");
		BasicMAPEKLiteLoopHelper.BUNDLECONTEXT = bundleContext;
		BasicMAPEKLiteLoopHelper.REFERENCE_MODEL = "AutonomousPlane"; //System.getProperty("model", "default-model");

		// ... adding the initial system configuration
		IComponentsSystemConfiguration theInitialSystemConfiguration = 
			    SystemConfigurationHelper.createSystemConfiguration("InitialConfiguration");

		//SystemConfigurationHelper.addComponent(theInitialSystemConfiguration, "device.RoadSensor", "1.0.0");
		BasicMAPEKLiteLoopHelper.INITIAL_SYSTEMCONFIGURATION = theInitialSystemConfiguration;
		
		BasicMAPEKLiteLoopHelper.MODELSREPOSITORY_FOLDER = System.getProperty("modelsrepository.folder");
		BasicMAPEKLiteLoopHelper.ADAPTATIONREPORTS_FOLDER = System.getProperty("adaptationreports.folder");
		// STARTING THE MAPE-K LOOP
		
		BasicMAPEKLiteLoopHelper.startLoopModules();

		
		//Configuracion inicial del sistema
		BasicMAPEKLiteLoopHelper.addInitialSelfConfigurationCapabilities(createMySystemConfiguration());
		
		// ADAPTATION PROPERTIES
		IKnowledgeProperty theAoaValue = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("aoa_value");
		IKnowledgeProperty theAutonomusType = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("autonomus_type");
		IKnowledgeProperty component_status = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("component_status");
		IKnowledgeProperty fallbackPlan = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("fallback_plan_type");
		IKnowledgeProperty thermal_status = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("thermal_status");
		IKnowledgeProperty humidty_status = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("humidity_level");
		IKnowledgeProperty fuel_distance = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("fuel_distance");
		IKnowledgeProperty distance_status = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("distance_remaining");
		IKnowledgeProperty weather_status = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("weather_status");
		IKnowledgeProperty terrainAltitude = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("terrain_altitude");
		IKnowledgeProperty flyingStage = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("flying_stage");
		IKnowledgeProperty verticalSpeed = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("vertical_speed");
		IKnowledgeProperty fuel_status = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("fuel_status");
		IKnowledgeProperty engineFailStatus = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("engine_failure_status");
		// ADAPTATION RULES
		IAdaptiveReadyComponent theADS_L1 = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L1(bundleContext));
		IAdaptiveReadyComponent theADS_EngineFail = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_EngineFailRule(bundleContext));
		IAdaptiveReadyComponent theADS_RoughWeather = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_RoughWeather(bundleContext));
		IAdaptiveReadyComponent theADS_RoughWeatherOFF = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_RoughWeatherOFF(bundleContext));

		IAdaptiveReadyComponent theADS_LowFuel = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_LowFuel(bundleContext));
		//IAdaptiveReadyComponent theADS_LowFuelOFF = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_LowFuelOFF(bundleContext));
					
		IAdaptiveReadyComponent theADS_ThermalFallbackPlan = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_ThermalFallbackPlan(bundleContext));
		IAdaptiveReadyComponent theADS_TAWSWarning = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_TAWSWarning(bundleContext));
		IAdaptiveReadyComponent theADS_Downgrade = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new autonomousplane.mapeklite.adaptationrules.ADS_Downgrade(bundleContext));
		IAdaptiveReadyComponent theADS_Replacements = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new autonomousplane.mapeklite.adaptationrules.ADS_Replacements(bundleContext));
		// MONITORS

		IAdaptiveReadyComponent AOAValueMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new AOAValueMonitor(bundleContext));
		IAdaptiveReadyComponent AutonomousTypeMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new AutonomousTypeMonitor(bundleContext));
		IAdaptiveReadyComponent componentStatusMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new ComponentStatusMonitor(bundleContext));
		IAdaptiveReadyComponent fallbackPlanMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new FallbackPlanTypeMonitor(bundleContext));
		IAdaptiveReadyComponent distanceMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new DistanceMonitor(bundleContext));
		IAdaptiveReadyComponent flyingStageMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new autonomousplane.mapeklite.monitors.FlyingStageMonitor(bundleContext));
		IAdaptiveReadyComponent fuelMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new autonomousplane.mapeklite.monitors.FuelDistanceMonitor(bundleContext));
		IAdaptiveReadyComponent weatherMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new autonomousplane.mapeklite.monitors.WeatherMonitor(bundleContext));
		IAdaptiveReadyComponent groundAltitudeMonitor = BasicMAPEKLiteLoopHelper.deployMonitor(new autonomousplane.mapeklite.monitors.GroundAltitudeMonitor(bundleContext));
		IAdaptiveReadyComponent humidityMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new autonomousplane.mapeklite.monitors.HumidityMonitor(bundleContext));
		IAdaptiveReadyComponent thermalMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new autonomousplane.mapeklite.monitors.ThermalMonitor(bundleContext));
		IAdaptiveReadyComponent verticalSpeedMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new VerticalSpeedMonitor(bundleContext));
		IAdaptiveReadyComponent fuelStatusMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new autonomousplane.mapeklite.monitors.FuelStatusMonitor(bundleContext));
		IAdaptiveReadyComponent engineFailMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new EngineMonitor(bundleContext));
		// PROBES
		IAdaptiveReadyComponent AOAValueProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new Aoaprobe(bundleContext), AOAValueMonitorARC);
		IAdaptiveReadyComponent AutonomousTypeProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new AutonomousProbe(bundleContext), AutonomousTypeMonitorARC);
		IAdaptiveReadyComponent ComponentStatusProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new ComponentStatusProbe(bundleContext), componentStatusMonitorARC);
		IAdaptiveReadyComponent fallbackPlanProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new FallbackPlanProbe(bundleContext), fallbackPlanMonitorARC);
		IAdaptiveReadyComponent distanceProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.DistanceProbe(bundleContext), distanceMonitorARC);
		IAdaptiveReadyComponent flyingStageProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.FlyingStageProbe(bundleContext), flyingStageMonitorARC);
		IAdaptiveReadyComponent fuelProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.FuelDistanceRemainingProbe(bundleContext), fuelMonitorARC);
		IAdaptiveReadyComponent weatherProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.WeatherProbe(bundleContext), weatherMonitorARC);
		IAdaptiveReadyComponent groundAltitudeProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.GroundAltitudeProbe(bundleContext), groundAltitudeMonitor);
		IAdaptiveReadyComponent humidityProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.HumidityProbe(bundleContext), humidityMonitorARC);
		IAdaptiveReadyComponent thermalProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.ThermalProbe(bundleContext), thermalMonitorARC);
		IAdaptiveReadyComponent verticalSpeedProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.VerticalSpeedProbe(bundleContext), verticalSpeedMonitorARC);
		IAdaptiveReadyComponent fuelStatusProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.FuelStatusProbe(bundleContext), fuelStatusMonitorARC);
		IAdaptiveReadyComponent engineProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new autonomousplane.mapeklite.probes.EngineProbe(bundleContext), engineFailMonitorARC);
		
		
		
		/*
		
		
		 
		// ADAPTATION PROPERTIES
		IKnowledgeProperty theRoadType = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("road_type");
		IKnowledgeProperty theAutonomusType = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("autonomus_type");
		IKnowledgeProperty theRoadStatus = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("road_status");
		IKnowledgeProperty theComponentStatus = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("component_status");
		IKnowledgeProperty theFaceStatus = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("face_status");
		IKnowledgeProperty theHandsOnWheel = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("hands_on_wheel");
		IKnowledgeProperty theSeatedOnPilot = BasicMAPEKLiteLoopHelper.createKnowledgeProperty("seated_on_pilot");

		// ADAPTATION RULES
 		IAdaptiveReadyComponent theADS_L3_1 = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_1(bundleContext));
 		IAdaptiveReadyComponent theADS_L3_2 = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_2(bundleContext));
 		IAdaptiveReadyComponent theADS_L3_3 = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_3(bundleContext));
 		IAdaptiveReadyComponent theADS_L3_4 = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_L3_4(bundleContext));
 		IAdaptiveReadyComponent theADS_2 = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new ADS_2(bundleContext));
 		IAdaptiveReadyComponent theINTERACT_1 = BasicMAPEKLiteLoopHelper.deployAdaptationRule(new INTERACT_1(bundleContext));
		// MONITORS
		IAdaptiveReadyComponent theRoadTypeMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new RoadTypeMonitor(bundleContext));
		IAdaptiveReadyComponent theAutonomousTypeMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new AutonomousTypeMonitor(bundleContext));
		IAdaptiveReadyComponent theComponentStatusMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new ComponentStatusMonitor(bundleContext));
		IAdaptiveReadyComponent theRoadStatusMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new RoadConditionMonitor(bundleContext));
		IAdaptiveReadyComponent theFaceStatusMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new FaceStatusMonitor(bundleContext));
		IAdaptiveReadyComponent theHandsOnWheelMonitorARC = BasicMAPEKLiteLoopHelper.deployMonitor(new HandsOnWheelMonitor(bundleContext));
		IAdaptiveReadyComponent theSeatedOnPilotARC = BasicMAPEKLiteLoopHelper.deployMonitor(new SeatedOnPilotMonitor(bundleContext));
		// PROBES
		IAdaptiveReadyComponent theRoadTypeProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new RoadTypeProbe(bundleContext), theRoadTypeMonitorARC);
		IAdaptiveReadyComponent theAutonomousTypeProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new AutonomousTypeProbe(bundleContext), theAutonomousTypeMonitorARC);
		IAdaptiveReadyComponent theComponentFailProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new ComponentFailProbe(bundleContext), theComponentStatusMonitorARC);
		IAdaptiveReadyComponent theRoadStatusProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new RoadStatusProbe(bundleContext), theRoadStatusMonitorARC);
		IAdaptiveReadyComponent theFaceStatusProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new FaceStatusProbe(bundleContext), theFaceStatusMonitorARC);
		IAdaptiveReadyComponent theHandsOnWheelProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new HandsOnWheelProbe(bundleContext), theHandsOnWheelMonitorARC);
		IAdaptiveReadyComponent theSeatedOnPilotProbeARC = BasicMAPEKLiteLoopHelper.deployProbe(new SeatedOnPilotProbe(bundleContext), theSeatedOnPilotARC);
		//*/
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	//Configuration inicial del sistema
	protected IRuleComponentsSystemConfiguration createMySystemConfiguration() {
		    IRuleComponentsSystemConfiguration theInitialSystemConfiguration =
		        SystemConfigurationHelper.createPartialSystemConfiguration("InitialConfiguration_" + ITimeStamped.getCurrentTimeStamp());

		    // Add components L0
	
		    //initializeL0(theInitialSystemConfiguration);
		    //L1
		
		    //initializeL1(theInitialSystemConfiguration);
		  
		    
		
		    //L2
		    //initializeL2(theInitialSystemConfiguration);
		    
		    //L3
		    initializeL3(theInitialSystemConfiguration);
		    
		    System.out.println("Initial system configuration created.");
		    return theInitialSystemConfiguration;
		}

	public void initializeL0(IRuleComponentsSystemConfiguration theInitialSystemConfiguration) {
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "autopilot.L0_ManualNavigation", "1.0.0");

	}
	public void initializeL1(IRuleComponentsSystemConfiguration theInitialSystemConfiguration) {
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.ControlSurfaces", "1.0.0");

	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AHRSSensor", "1.0.0");

	    
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "autopilot.L1_BasicNavigationAssistance", "1.0.0");
	    
	    
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L1_BasicNavigationAssistance", "1.0.0",
	    	    L1_FlyingServiceARC.REQUIRED_AHRSSENSOR,
	    	    "device.AHRSSensor", "1.0.0",
	    	    AHRSSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L1_BasicNavigationAssistance", "1.0.0",
	    	    L1_FlyingServiceARC.REQUIRED_CONTROLSURFACES,
	    	    "device.ControlSurfaces", "1.0.0",
	    	    ControlSurfacesARC.PROVIDED_DEVICE
	    );
	
		
	}
	
	public void initializeL2(IRuleComponentsSystemConfiguration theInitialSystemConfiguration) {
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.ControlSurfaces", "1.0.0");

	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AHRSSensor", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.NotificationService", "1.0.0");

	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "autopilot.L2_PartialAutomation", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AOASensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.StallWarning", "1.0.0");

	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.GNSS", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AltitudeSensor", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RadioAltimeterSensor", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.SpeedSensor", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.FADEC", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.WeatherSensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.GeneralWarning", "1.0.0");

	    SystemConfigurationHelper.bindingToAdd(
		        theInitialSystemConfiguration,
		        "autopilot.L2_PartialAutomation", "1.0.0",
		        L2_FlyingServiceARC.REQUIRED_AOASENSOR,
		        "device.AOASensor", "1.0.0",
		        AOASensorARC.PROVIDED_DEVICE
		    );
	    
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_NOTIFICATIONSERVICE,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.PROVIDED_SERVICE
	    	);
	    
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_AHRSSENSOR,
	    	    "device.AHRSSensor", "1.0.0",
	    	    AHRSSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_CONTROLSURFACES,
	    	    "device.ControlSurfaces", "1.0.0",
	    	    ControlSurfacesARC.PROVIDED_DEVICE
	    );
	  
	 /*   SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.REQUIRED_SERVICE,
	    	    "interaction.TAWS", "1.0.0",
	    	    TAWSARC.PROVIDED_MECHANISM
	    	);*/
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_NAVIGATIONSYSTEM,
	    	    "device.GNSS", "1.0.0",
	    	    NavigationSystemARC.PROVIDED_DEVICE
	    );
	
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_ALTIMETERSENSOR,
	    	    "device.AltitudeSensor", "1.0.0",
	    	    AltitudeSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_RADIOALTIMETERSENSOR,
	    	    "device.RadioAltimeterSensor", "1.0.0",
	    	    RadioAltimeterSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_SPEEDSENSOR,
	    	    "device.SpeedSensor", "1.0.0",
	    	    SpeedSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_FADEC,
	    	    "device.FADEC", "1.0.0",
	    	    FADECARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "autopilot.L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIERED_WEATHERSENSOR,
	    	    "device.WeatherSensor", "1.0.0",
	    	    WeatherSensorARC.PROVIDED_DEVICE
	    );
	    
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.REQUIRED_SERVICE,
	    	    "interaction.GeneralWarning", "1.0.0",
	    	    GeneralWarningARC.PROVIDED_MECHANISM
	    	);
	    // Fallback Plan
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.StallWarning", "1.0.0");
		SystemConfigurationHelper.bindingToAdd(
				theInitialSystemConfiguration,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.REQUIRED_SERVICE,
	    	    "interaction.StallWarning", "1.0.0",
	    	    StallWarningARC.PROVIDED_MECHANISM
	    	);
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "autopilot.StallRecoveryFallbackPlan", "1.0.0");
	  	
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    StallRecoveryFallbackPlanARC.REQUIRED_NOTIFICATIONSERVICE,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.PROVIDED_SERVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    StallRecoveryFallbackPlanARC.REQUIRED_ATTITUDESENSOR,
	    	    "device.AHRSSensor", "1.0.0",
	    	    AHRSSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    StallRecoveryFallbackPlanARC.REQUIRED_SPEEDSENSOR,
	    	    "device.SpeedSensor", "1.0.0",
	    	    SpeedSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    StallRecoveryFallbackPlanARC.REQUIRED_ALTITUDESENSOR,
	    	    "device.AltitudeSensor", "1.0.0",
	    	    AltitudeSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    StallRecoveryFallbackPlanARC.REQUIRED_FADEC,
	    	    "device.FADEC", "1.0.0",
	    	    FADECARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    StallRecoveryFallbackPlanARC.REQUIRED_CONTROLSURFACE,
	    	    "device.ControlSurfaces", "1.0.0",
	    	    ControlSurfacesARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    StallRecoveryFallbackPlanARC.REQUIRED_AOASENSOR,
	    	    "device.AOASensor", "1.0.0",
		        AOASensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    StallRecoveryFallbackPlanARC.REQUIRED_WEATHERSENSOR,
	    	    "device.WeatherSensor", "1.0.0",
	    	    WeatherSensorARC.PROVIDED_DEVICE
	    );
	
	
		    SystemConfigurationHelper.bindingToAdd(
		    		theInitialSystemConfiguration,
		    	    "autopilot.L2_PartialAutomation", "1.0.0",
		    	    L2_FlyingServiceARC.REQUIERED_FALLBACKPLAN,
		    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
		    	    FallbackPlanARC.PROVIDED_FLYINGSERVICE
		    );
	}
	
	public void initializeL3(IRuleComponentsSystemConfiguration theInitialSystemConfiguration) {
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.ControlSurfaces", "1.0.0");

		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AHRSSensor", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.NotificationService", "1.0.0");

		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AOASensor", "1.0.0");
			SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.StallWarning", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "autopilot.L3_AdvancedAutomation", "1.0.0");

		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.GNSS", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AltitudeSensor", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RadioAltimeterSensor", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.SpeedSensor", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.FADEC", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.WeatherSensor", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.CapacitiveFuelSensor", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.ILSSystem", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.LIDARSensor", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.EGTSensor", "1.0.0");
			SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "autopilot.ThermalFallbackPlan", "1.0.0");
			SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.GeneralWarning", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.OverheatWarning", "1.0.0");
			SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.FrozenWarning", "1.0.0");
			
			
		
			
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_FUELSENSOR,
			        "device.CapacitiveFuelSensor", "1.0.0",
			        FuelSensorARC.PROVIDED_SENSOR
			    );
			 
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_EGTSENSOR,
			        "device.EGTSensor", "1.0.0",
			        EGTSensorARC.PROVIDED_SENSOR
			    );
			
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_PROXIMITYSENSOR,
			        "device.LIDARSensor", "1.0.0",
			        ProximitySensorARC.PROVIDED_DEVICE
			    );
			
			 
			    SystemConfigurationHelper.bindingToAdd(
				        theInitialSystemConfiguration,
				        "autopilot.L3_AdvancedAutomation", "1.0.0",
				        L3_FlyingServiceARC.REQUIRED_LANDINGSYSTEM,
				        "device.ILSSystem", "1.0.0",
				        LandingSystemARC.PROVIDED_DEVICE
				    );
			
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_AOASENSOR,
			        "device.AOASensor", "1.0.0",
			        AOASensorARC.PROVIDED_DEVICE
			    );
		    
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_NOTIFICATIONSERVICE,
		    	    "interaction.NotificationService", "1.0.0",
		    	    NotificationServiceARC.PROVIDED_SERVICE
		    	);
		    
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_AHRSSENSOR,
		    	    "device.AHRSSensor", "1.0.0",
		    	    AHRSSensorARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_CONTROLSURFACES,
		    	    "device.ControlSurfaces", "1.0.0",
		    	    ControlSurfacesARC.PROVIDED_DEVICE
		    );
		  /*		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "interaction.NotificationService", "1.0.0",
		    	    NotificationServiceARC.REQUIRED_SERVICE,
		    	    "interaction.TAWS", "1.0.0",
		    	    TAWSARC.PROVIDED_MECHANISM
		    	);*/
			    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_NAVIGATIONSYSTEM,
		    	    "device.GNSS", "1.0.0",
		    	    NavigationSystemARC.PROVIDED_DEVICE
		    );
		  
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_ALTIMETERSENSOR,
		    	    "device.AltitudeSensor", "1.0.0",
		    	    AltitudeSensorARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_RADIOALTIMETERSENSOR,
		    	    "device.RadioAltimeterSensor", "1.0.0",
		    	    RadioAltimeterSensorARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_SPEEDSENSOR,
		    	    "device.SpeedSensor", "1.0.0",
		    	    SpeedSensorARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_FADEC,
		    	    "device.FADEC", "1.0.0",
		    	    FADECARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "autopilot.L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIERED_WEATHERSENSOR,
		    	    "device.WeatherSensor", "1.0.0",
		    	    WeatherSensorARC.PROVIDED_DEVICE
		    );
		    // Fallback Plan
			
		    SystemConfigurationHelper.bindingToAdd(
				  	theInitialSystemConfiguration,
		    	    "interaction.NotificationService", "1.0.0",
		    	    NotificationServiceARC.REQUIRED_SERVICE,
		    	    "interaction.OverheatWarning", "1.0.0",
		    	    OverheatWarningARC.PROVIDED_MECHANISM
		    	);
	

			SystemConfigurationHelper.bindingToAdd(
				  	theInitialSystemConfiguration,
		    	    "interaction.NotificationService", "1.0.0",
		    	    NotificationServiceARC.REQUIRED_SERVICE,
		    	    "interaction.FrozenWarning", "1.0.0",
		    	    FrozenWarningARC.PROVIDED_MECHANISM
		    	);
		
		
	
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.ThermalFallbackPlan", "1.0.0",
	    	    ThermalFallbackPlanARC.REQUIRED_NOTIFICATIONSERVICE,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.PROVIDED_SERVICE
	    );
		
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.ThermalFallbackPlan", "1.0.0",
	    	    ThermalFallbackPlanARC.REQUIRED_WEATHERSENSOR,
	    	    "device.WeatherSensor", "1.0.0",
	    	    WeatherSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.ThermalFallbackPlan", "1.0.0",
	    	    ThermalFallbackPlanARC.REQUIRED_EGTSENSOR,
	    	    "device.EGTSensor", "1.0.0",
	    	    EGTSensorARC.PROVIDED_SENSOR
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    		theInitialSystemConfiguration,
	    	    "autopilot.ThermalFallbackPlan", "1.0.0",
	    	    ThermalFallbackPlanARC.REQUIRED_FADEC,
	    	    "device.FADEC", "1.0.0",
	    	    FADECARC.PROVIDED_DEVICE
	    );
		
		SystemConfigurationHelper.bindingToAdd(
				theInitialSystemConfiguration,
				"autopilot.L3_AdvancedAutomation", "1.0.0",
		        L3_FlyingServiceARC.REQUIERED_FALLBACKPLAN,
	    	    "autopilot.ThermalFallbackPlan", "1.0.0",
	    	    ThermalFallbackPlanARC.PROVIDED_FLYINGSERVICE);
		
		}
	
		
	
	

}