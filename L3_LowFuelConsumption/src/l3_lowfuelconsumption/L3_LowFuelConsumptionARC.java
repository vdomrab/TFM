package l3_lowfuelconsumption;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL3_AdvancedAutomation;
import autonomousplane.autopilot.interfaces.IL3_IntenseWeatherNavigation;
import autonomousplane.autopilot.interfaces.IL3_LowFuelConsumption;
import autonomousplane.infraestructure.autopilotARC.L3_FlyingServiceARC;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L3_LowFuelConsumptionARC extends L3_FlyingServiceARC {

	public L3_LowFuelConsumptionARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new L3_LowFuelConsumption(this.context, id));
	}

	protected IL3_LowFuelConsumption getTheL3_AdvancedAutomationService() {
		return (IL3_LowFuelConsumption) this.getTheFlyingService();
	}

}
