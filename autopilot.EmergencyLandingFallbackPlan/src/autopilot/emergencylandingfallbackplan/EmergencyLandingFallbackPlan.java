package autopilot.emergencylandingfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IEmergencyLandingFallbackPlan;
import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IETL;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.autopilot.FallbackPlan;

public class EmergencyLandingFallbackPlan extends FallbackPlan implements IEmergencyLandingFallbackPlan {

	private IRadioAltimeterSensor radioAltimeter = null;
	private IControlSurfaces controlSurfaces = null;
	private IAttitudeSensor attitudeSensor = null;
	private IAltitudeSensor altitudeSensor = null;
	private IETL etlSensor = null;
	private ISpeedSensor speedSensor = null;
	private IProximitySensor proximitySensor = null;
	private IFADEC fadec = null;
	private ILandingSystem landingSystem = null;
	private IFuelSensor fuelSensor = null;
	private IWeatherSensor weatherSensor = null;
	public EmergencyLandingFallbackPlan(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		this.addImplementedInterface(IEmergencyLandingFallbackPlan.class.getName());
	}
	
	public void setRadioAltimeter(IRadioAltimeterSensor radioAltimeter) {
		this.radioAltimeter = radioAltimeter;
	}
	@Override
	public void setControlSurfaces(IControlSurfaces controlSurfaces) {
		this.controlSurfaces = controlSurfaces;
	}

	@Override
	public void setAttitudeSensor(IAttitudeSensor attitudeSensor) {
		this.attitudeSensor = attitudeSensor;
	}

	@Override
	public void setAltitudeSensor(IAltitudeSensor altitudeSensor) {
		this.altitudeSensor = altitudeSensor;
	}

	@Override
	public void setETLSensor(IETL etlSensor) {
		this.etlSensor = etlSensor;
	}

	@Override
	public void setSpeedSensor(ISpeedSensor speedSensor) {
		this.speedSensor = speedSensor;
	}
	
	@Override
	public void setProximitySensor(IProximitySensor proximitySensor) {
		this.proximitySensor = proximitySensor;
	}
	
	@Override
	public void setFADEC(IFADEC fadec) {
		this.fadec = fadec;
	}
	@Override
	public void setFuelSensor(IFuelSensor fuelSensor) {
		this.fuelSensor = fuelSensor;
	}
	@Override
	public void setLandingSystem(ILandingSystem landingSystem) {
		this.landingSystem = landingSystem;
	}
	public void setWeatherSensor(IWeatherSensor weatherSensor) {
		this.weatherSensor = weatherSensor;
	}
	@Override
	public IFlyingService performTheFlyingFunction() {
		// Implement the logic for the thermal fallback plan
		

		// Example logic: Use fadec, weatherSensor, and egtSensor to perform actions
		logger.info("Performing thermal fallback actions with FADEC, EGT Sensor, and Weather Sensor.");
		
		// Return the flying service or perform necessary actions
		return this; // Assuming this is the flying service being returned
	}
	
	@Override
	public IFlyingService stopTheFlyingFunction() {
		logger.info("Emergency Landing Completed.");
		return this;
	}
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean ok = true;
		if(speedSensor == null) {
			logger.error("GlideToSafetyFallbackPlan: Speed Sensor service is not set.");
			ok = false;
		} else if (radioAltimeter == null) {
			logger.error("GlideToSafetyFallbackPlan: Radio Altimeter service is not set.");
			ok = false;
		} else if (controlSurfaces == null) {
			logger.error("GlideToSafetyFallbackPlan: Control Surfaces service is not set.");
			ok = false;
		} else if (attitudeSensor == null) {
			logger.error("GlideToSafetyFallbackPlan: Attitude Sensor service is not set.");
			ok = false;
		} else if (altitudeSensor == null) {
			logger.error("GlideToSafetyFallbackPlan: Altitude Sensor service is not set.");
			ok = false;
		} else if (etlSensor == null) {
			logger.error("GlideToSafetyFallbackPlan: ETL Sensor service is not set.");
			ok = false;
		}else if (fadec == null) {
			logger.error("EmergencyLandingFallbackPlan: FADEC service is not set.");
			ok = false;
		} else if (fuelSensor == null) {
			logger.error("EmergencyLandingFallbackPlan: Fuel Sensor service is not set.");
			ok = false;
		} else if (landingSystem == null) {
			logger.error("EmergencyLandingFallbackPlan: Landing System service is not set.");
			ok = false;
		} else if (weatherSensor == null) {
			logger.error("EmergencyLandingFallbackPlan: Weather Sensor service is not set.");
			ok = false;
		}
			
			
		return ok;
	}
	
	
	// Implement other methods from IEmergencyLandingFallbackPlan here...

}
