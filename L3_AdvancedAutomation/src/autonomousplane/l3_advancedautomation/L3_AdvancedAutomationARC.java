package autonomousplane.l3_advancedautomation;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL3_AdvancedAutomation;
import autonomousplane.infraestructure.autopilotARC.L3_FlyingServiceARC;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L3_AdvancedAutomationARC extends L3_FlyingServiceARC {

	public L3_AdvancedAutomationARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new L3_AdvancedAutomation(this.context, id));
	}

	protected IL3_AdvancedAutomation getTheL3_AdvancedAutomationService() {
		return (IL3_AdvancedAutomation) this.getTheFlyingService();
	}

}
