package autonomousplane.l1_basicnavigationassistance;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL1_BasicNavigationAssistance;
import autonomousplane.infraestructure.autopilotARC.L1_FlyingServiceARC;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L1_BasicNavigationAssistanceARC extends L1_FlyingServiceARC {

	public L1_BasicNavigationAssistanceARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new L1_BasicNavigationAssistance(this.context, id));
	}

	protected IL1_BasicNavigationAssistance getTheL1_BasicNavigationAssistanceService() {
		return (IL1_BasicNavigationAssistance) this.getTheFlyingService();
	}

}
