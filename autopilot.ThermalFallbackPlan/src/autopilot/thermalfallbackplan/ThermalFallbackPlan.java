package autopilot.thermalfallbackplan;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IThermalFallbackPlan;
import autonomousplane.infraestructure.autopilot.FallbackPlan;
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
		// Implement the logic for the thermal fallback plan
		if (fadec == null || weatherSensor == null || egtSensor == null) {
			logger.error("Required components are not set for Thermal Fallback Plan.");
			return null; // or throw an exception
		}

		// Example logic: Use fadec, weatherSensor, and egtSensor to perform actions
		logger.info("Performing thermal fallback actions with FADEC, EGT Sensor, and Weather Sensor.");
		
		// Return the flying service or perform necessary actions
		return this; // Assuming this is the flying service being returned
	}
	
	@Override
	public IFlyingService stopTheFlyingFunction() {
		logger.info("Stall recovery completed.");
		return this;
	}
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean ok = true;
		if (this.fadec == null) {
			logger.error("StallRecoveryFallbackPlan: FADEC service is not set.");
			ok = false;
		}else if (this.weatherSensor == null) {
			logger.error("StallRecoveryFallbackPlan: Weather Sensor service is not set.");
			ok = false;
		} else if (this.egtSensor == null) {
			logger.error("StallRecoveryFallbackPlan: EGT Sensor service is not set.");
			ok = false;
		}
		
		return ok;
	}
}
