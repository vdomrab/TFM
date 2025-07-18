package autonomousplane.infraestructure.interaction;

import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.Thing;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;
import autonomousplane.interaction.interfaces.IInteractionMechanism;

public abstract class InteractionMechanism extends Thing implements IInteractionMechanism {
	
	protected SmartLogger logger = SmartLogger.getLogger(InteractionMechanism.class);

	public InteractionMechanism(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IInteractionMechanism.class.getName());
	}
	
	protected void showMessage(String message) {
		logger.info(String.format("## %30s ## %s", this.getId(), message));
	}

}
