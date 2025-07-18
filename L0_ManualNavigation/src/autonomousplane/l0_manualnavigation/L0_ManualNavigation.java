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
		logger.info("Performing manual navigation... ");
		/*if ( this.getAOASensor().getAOA() > 12 && this.getAOASensor().getAOA() < 15 ) {
			correction_required = true;
			logger.info("Plane approaching stall condition");
			if ( this.getNotificationService() != null )
				this.getNotificationService().notify("Plane approaching stall condition: " + this.getAOASensor().getAOA() );
		}else if ( this.getAOASensor().getAOA() >= 15) {
			correction_required = true;
			logger.info("Plane is in stall condition");
			if ( this.getNotificationService() != null )
				this.getNotificationService().notify("Plane is in stall condition: " + this.getAOASensor().getAOA() );
		}
		*/
		
		if ( !correction_required ) {
			logger.info("Monitoring flying parameters. Nothing to warn ...");
		}
		
		return this;

	}

}
