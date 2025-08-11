package autonomousplane.l0_manualnavigation;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IL0_ManualNavigation;
import autonomousplane.infraestructure.autopilot.L0_FlyingService;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class L0_ManualNavigation extends L0_FlyingService implements IL0_ManualNavigation {

	public L0_ManualNavigation(BundleContext context, String id) {
		super(context, id);
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
	}

	@Override
	public IFlyingService performTheFlyingFunction() {
		boolean correction_required = false;
		logger.info("Performing L0 manual navigation... ");
		
		
		if ( !correction_required ) {
			logger.info("Monitoring flying parameters. Nothing to warn ...");
		}
		
		return this;

	}

}
