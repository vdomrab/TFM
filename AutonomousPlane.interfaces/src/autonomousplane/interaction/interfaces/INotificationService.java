package autonomousplane.interaction.interfaces;

import autonomousplane.interfaces.IIdentifiable;

public interface INotificationService extends IIdentifiable {
	
	public INotificationService notify(String message, String... mechanisms);
	
	public INotificationService addInteractionMechanism(String mechanism);
	public INotificationService removeInteractionMechanism(String mechanism);
	public boolean isMechanismAvailable(String id);

}
