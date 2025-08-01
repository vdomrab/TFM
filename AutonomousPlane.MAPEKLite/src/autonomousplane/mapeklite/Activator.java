package autonomousplane.mapeklite;



import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
import autonomousplane.infraestructure.interaction.ARC.NotificationServiceARC;
import autonomousplane.infraestructure.interaction.ARC.StallWarningARC;
import autonomousplane.infraestructure.interaction.ARC.TAWSARC;
import autopilot.stallrecoveryfallbackplan.StallRecoveryFallbackPlanARC;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IComponentsSystemConfiguration;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.structures.systemconfiguration.interfaces.IRuleComponentsSystemConfiguration;
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
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "L0_ManualNavigation", "1.0.0");

	}
	public void initializeL1(IRuleComponentsSystemConfiguration theInitialSystemConfiguration) {
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.ControlSurfaces", "1.0.0");

	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AHRSSensor", "1.0.0");

	    
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "L1_BasicNavigationAssistance", "1.0.0");
	    
	    
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L1_BasicNavigationAssistance", "1.0.0",
	    	    L1_FlyingServiceARC.REQUIRED_AHRSSENSOR,
	    	    "device.AHRSSensor", "1.0.0",
	    	    AHRSSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L1_BasicNavigationAssistance", "1.0.0",
	    	    L1_FlyingServiceARC.REQUIRED_CONTROLSURFACES,
	    	    "device.ControlSurfaces", "1.0.0",
	    	    ControlSurfacesARC.PROVIDED_DEVICE
	    );
	
		
	}
	
	public void initializeL2(IRuleComponentsSystemConfiguration theInitialSystemConfiguration) {
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.ControlSurfaces", "1.0.0");

	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AHRSSensor", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.NotificationService", "1.0.0");

	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "L2_PartialAutomation", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AOASensor", "1.0.0");
		SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.StallWarning", "1.0.0");

	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.GNSS", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AltitudeSensor", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.RadioAltimeterSensor", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.SpeedSensor", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.FADEC", "1.0.0");
	    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.WeatherSensor", "1.0.0");

	    SystemConfigurationHelper.bindingToAdd(
		        theInitialSystemConfiguration,
		        "L2_PartialAutomation", "1.0.0",
		        L2_FlyingServiceARC.REQUIRED_AOASENSOR,
		        "device.AOASensor", "1.0.0",
		        AOASensorARC.PROVIDED_DEVICE
		    );
	    
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_NOTIFICATIONSERVICE,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.PROVIDED_SERVICE
	    	);
	    
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_AHRSSENSOR,
	    	    "device.AHRSSensor", "1.0.0",
	    	    AHRSSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_CONTROLSURFACES,
	    	    "device.ControlSurfaces", "1.0.0",
	    	    ControlSurfacesARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.REQUIRED_SERVICE,
	    	    "interaction.StallWarning", "1.0.0",
	    	    StallWarningARC.PROVIDED_MECHANISM
	    	);
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "interaction.NotificationService", "1.0.0",
	    	    NotificationServiceARC.REQUIRED_SERVICE,
	    	    "interaction.TAWS", "1.0.0",
	    	    TAWSARC.PROVIDED_MECHANISM
	    	);
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_NAVIGATIONSYSTEM,
	    	    "device.GNSS", "1.0.0",
	    	    NavigationSystemARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_ALTIMETERSENSOR,
	    	    "device.AltitudeSensor", "1.0.0",
	    	    AltitudeSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_RADIOALTIMETERSENSOR,
	    	    "device.RadioAltimeterSensor", "1.0.0",
	    	    RadioAltimeterSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_SPEEDSENSOR,
	    	    "device.SpeedSensor", "1.0.0",
	    	    SpeedSensorARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIRED_FADEC,
	    	    "device.FADEC", "1.0.0",
	    	    FADECARC.PROVIDED_DEVICE
	    );
	    SystemConfigurationHelper.bindingToAdd(
	    	    theInitialSystemConfiguration,
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIERED_WEATHERSENSOR,
	    	    "device.WeatherSensor", "1.0.0",
	    	    WeatherSensorARC.PROVIDED_DEVICE
	    );
	    // Fallback Plan
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
	    	    "L2_PartialAutomation", "1.0.0",
	    	    L2_FlyingServiceARC.REQUIERED_FALLBACKPLAN,
	    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
	    	    FallbackPlanARC.PROVIDED_FLYINGSERVICE
	    );
	}
	
	public void initializeL3(IRuleComponentsSystemConfiguration theInitialSystemConfiguration) {
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.ControlSurfaces", "1.0.0");

		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AHRSSensor", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.NotificationService", "1.0.0");

		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "L3_AdvancedAutomation", "1.0.0");
		    SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "device.AOASensor", "1.0.0");
			SystemConfigurationHelper.componentToAdd(theInitialSystemConfiguration, "interaction.StallWarning", "1.0.0");
			
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

		    
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_FUELSENSOR,
			        "device.CapacitiveFuelSensor", "1.0.0",
			        FuelSensorARC.PROVIDED_SENSOR
			    );
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_EGTSENSOR,
			        "device.EGTSensor", "1.0.0",
			        EGTSensorARC.PROVIDED_SENSOR
			    );
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_PROXIMITYSENSOR,
			        "device.LIDARSensor", "1.0.0",
			        ProximitySensorARC.PROVIDED_DEVICE
			    );
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_LANDINGSYSTEM,
			        "device.ILSSystem", "1.0.0",
			        LandingSystemARC.PROVIDED_DEVICE
			    );
		    SystemConfigurationHelper.bindingToAdd(
			        theInitialSystemConfiguration,
			        "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_AOASENSOR,
			        "device.AOASensor", "1.0.0",
			        AOASensorARC.PROVIDED_DEVICE
			    );
		    
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_NOTIFICATIONSERVICE,
		    	    "interaction.NotificationService", "1.0.0",
		    	    NotificationServiceARC.PROVIDED_SERVICE
		    	);
		    
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_AHRSSENSOR,
		    	    "device.AHRSSensor", "1.0.0",
		    	    AHRSSensorARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_CONTROLSURFACES,
		    	    "device.ControlSurfaces", "1.0.0",
		    	    ControlSurfacesARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "interaction.NotificationService", "1.0.0",
		    	    NotificationServiceARC.REQUIRED_SERVICE,
		    	    "interaction.StallWarning", "1.0.0",
		    	    StallWarningARC.PROVIDED_MECHANISM
		    	);
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "interaction.NotificationService", "1.0.0",
		    	    NotificationServiceARC.REQUIRED_SERVICE,
		    	    "interaction.TAWS", "1.0.0",
		    	    TAWSARC.PROVIDED_MECHANISM
		    	);
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_NAVIGATIONSYSTEM,
		    	    "device.GNSS", "1.0.0",
		    	    NavigationSystemARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_ALTIMETERSENSOR,
		    	    "device.AltitudeSensor", "1.0.0",
		    	    AltitudeSensorARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_RADIOALTIMETERSENSOR,
		    	    "device.RadioAltimeterSensor", "1.0.0",
		    	    RadioAltimeterSensorARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_SPEEDSENSOR,
		    	    "device.SpeedSensor", "1.0.0",
		    	    SpeedSensorARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIRED_FADEC,
		    	    "device.FADEC", "1.0.0",
		    	    FADECARC.PROVIDED_DEVICE
		    );
		    SystemConfigurationHelper.bindingToAdd(
		    	    theInitialSystemConfiguration,
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIERED_WEATHERSENSOR,
		    	    "device.WeatherSensor", "1.0.0",
		    	    WeatherSensorARC.PROVIDED_DEVICE
		    );
		    // Fallback Plan
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
		    	    "L3_AdvancedAutomation", "1.0.0",
			        L3_FlyingServiceARC.REQUIERED_FALLBACKPLAN,
		    	    "autopilot.StallRecoveryFallbackPlan", "1.0.0",
		    	    FallbackPlanARC.PROVIDED_FLYINGSERVICE
		    );
		}
		
	
	

}