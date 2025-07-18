package autonomousplane.l0_manualnavigation;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL0_ManualNavigation;
import autonomousplane.infraestructure.autopilotARC.L0_FlyingServiceARC;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L0_ManualNavigationARC extends L0_FlyingServiceARC {

	public L0_ManualNavigationARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.setTheFlyingService(new L0_ManualNavigation(this.context, id));
	}

	public IL0_ManualNavigation getTheL0_ManualNavigationService() {
		return (IL0_ManualNavigation) this.getTheFlyingService();
	}

}
