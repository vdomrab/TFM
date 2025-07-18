package autonomousplane.infraestructure.autopilot;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IL0_FlyingService;
import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.interaction.interfaces.INotificationService;

public abstract class L0_FlyingService extends FlyingService implements IL0_FlyingService {

	public L0_FlyingService(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IL0_FlyingService.class.getName());
	}
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		return true;
	}

	@Override
	public IFlyingService stopTheFlyingFunction() {
		return this;
	}

}
