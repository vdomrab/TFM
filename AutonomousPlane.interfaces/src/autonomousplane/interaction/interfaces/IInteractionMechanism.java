package autonomousplane.interaction.interfaces;

import autonomousplane.devices.interfaces.IThing;

public interface IInteractionMechanism extends IThing {

	public IInteractionMechanism performTheInteraction(String message);
	
}
