package l3_intenseweathernavigation;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL3_AdvancedAutomation;
import autonomousplane.autopilot.interfaces.IL3_IntenseWeatherNavigation;
import autonomousplane.infraestructure.autopilotARC.L3_FlyingServiceARC;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L3_IntenseWeatherNavigationARC extends L3_FlyingServiceARC {

	public L3_IntenseWeatherNavigationARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new L3_IntenseWeatherNavigation(this.context, id));
	}

	protected IL3_IntenseWeatherNavigation getTheL3_AdvancedAutomationService() {
		return (IL3_IntenseWeatherNavigation) this.getTheFlyingService();
	}

}
