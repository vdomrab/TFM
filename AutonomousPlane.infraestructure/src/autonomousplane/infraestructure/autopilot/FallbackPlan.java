package autonomousplane.infraestructure.autopilot;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFallbackPlan;
import autonomousplane.interaction.interfaces.INotificationService;

public abstract class FallbackPlan extends FlyingService implements IFallbackPlan {

	private INotificationService notificationService;
	
	public FallbackPlan(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IFallbackPlan.class.getName());
	}
	@Override
	public void setNotificationService(INotificationService service) {
		this.notificationService = service;
	}

	protected INotificationService getNotificationService() {
		return this.notificationService;
	}
	
	
	
	
	// Additional methods and logic for the fallback plan can be added here

}
