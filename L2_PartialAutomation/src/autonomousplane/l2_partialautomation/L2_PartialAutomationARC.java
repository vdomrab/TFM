package autonomousplane.l2_partialautomation;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL2_PartialAutomation;
import autonomousplane.infraestructure.autopilotARC.L2_FlyingServiceARC;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L2_PartialAutomationARC extends L2_FlyingServiceARC {

	public L2_PartialAutomationARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new L2_PartialAutomation(this.context, id));
	}

	protected IL2_PartialAutomation getTheL2_PartialAutomationService() {
		return (IL2_PartialAutomation) this.getTheFlyingService();
	}

}
