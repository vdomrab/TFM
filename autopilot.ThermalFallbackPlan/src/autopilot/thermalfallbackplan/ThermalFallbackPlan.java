package autopilot.thermalfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IThermalFallbackPlan;
import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.autopilot.FallbackPlan;
import autonomousplane.infraestructure.autopilot.FlyingService;
import autonomousplane.infraestructure.devices.EGTSensor;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.devices.interfaces.IEGTSensor;

public class ThermalFallbackPlan extends FallbackPlan implements IThermalFallbackPlan {
	// Attributes for the thermal fallback plan
	private IFADEC fadec = null;
	private IWeatherSensor weatherSensor = null;
	private IEGTSensor egtSensor = null;

	public ThermalFallbackPlan(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.addImplementedInterface(IThermalFallbackPlan.class.getName());
	}

	@Override
	public void setFADEC(IFADEC fadec) {
		this.fadec = fadec;
	}

	@Override
	public void setWeatherSensor(IWeatherSensor weatherSensor) {
		this.weatherSensor = weatherSensor;
	}

	@Override
	public void setEGTSensor(IEGTSensor egtSensor) {
		this.egtSensor = egtSensor;
	}

	@Override
	public IFlyingService performTheFlyingFunction() {
		logger.info("Thermal Fallback Plan: Performing thermal recovery maneuvers.");

		// Implement the logic for the thermal fallback plan
		if (checkRequirementsToPerformTheFlyingService() == false) {
			logger.error("Required components are not set for Thermal Fallback Plan.");
			return null; // or throw an exception
		}
		if (this.egtSensor.getTemperature() > EGTSensor.OVERHEAT_THRESHOLD_C) {
				 this.egtSensor.setHeatingEnabled(false); // Activate cooling system
	             this.egtSensor.setCoolingEnabled(true);
	             this.fadec.setTHRUSTPercentage(75); // Reduce thrust to 75% to prevent further overheating
			 
	     } else if (this.egtSensor.getTemperature() < EGTSensor.ICE_DANGER_OAT_THRESHOLD_C && this.weatherSensor.getHumidity() > 70.0) {
             this.egtSensor.setCoolingEnabled(false);
	    	 this.egtSensor.setHeatingEnabled(true);
             this.fadec.setTHRUSTPercentage(fadec.getCurrentThrust() + 20); // Reduce thrust to 75% to prevent further overheating

	             
	    }else {
	    	 this.egtSensor.setCoolingEnabled(false);
	    	 this.egtSensor.setHeatingEnabled(false);
	    	 IFlyingService flyingService = OSGiUtils.getService(
		        	    context,
		        	    IFlyingService.class,
		        	    "(&" +
		        	      "(!(id=autopilot.StallRecoveryFallbackPlan))" +
		        	      "(!(id=autopilot.ThermalFallbackPlan))" +
		        	    ")"
		        	);



	    	 if (flyingService != null) {
	    		    flyingService.startFlight();
	    		    this.stopTheFlyingFunction();
	    		    System.out.println("Thermal recovery completed, resuming normal flight.");
	    		    return flyingService; // <-- Return the normal flying service
	    		} else {
	    		    logger.error("Thermal Fallback Plan: No IFlyingService available to continue normal flight.");
	    		    return this;
	    		}
	    }
		
		// Return the flying service or perform necessary actions
		return this; // Assuming this is the flying service being returned
	}
	
	@Override
	public IFlyingService stopTheFlyingFunction() {
		logger.info("Thermal recovery completed.");
		this.setProperty(FlyingService.ACTIVE, false);
		return this;
	}
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean ok = true;
		if (this.fadec == null) {
			logger.error("ThermalFallbackPlan: FADEC service is not set.");
			ok = false;
		}else if (this.weatherSensor == null) {
			logger.error("ThermalFallbackPlan: Weather Sensor service is not set.");
			ok = false;
		} else if (this.egtSensor == null) {
			logger.error("ThermalFallbackPlan: EGT Sensor service is not set.");
			ok = false;
		}
		
		return ok;
	}
}
