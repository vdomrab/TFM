package autonomousplane.autopilot.interfaces;

import autonomousplane.interaction.interfaces.INotificationService;

public interface IFallbackPlan extends IFlyingService {
	public void setNotificationService(INotificationService service);

}
