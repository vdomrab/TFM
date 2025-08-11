package autonomousplane.infraestructure.interaction;

import org.osgi.framework.BundleContext;

public class StallWarning extends InteractionMechanism {

	public StallWarning(BundleContext context, String device)  {
		super(context, String.format("%s", device));
	}

	@Override
	public StallWarning performTheInteraction(String message) {
		this.showMessage("🛑 " + message);
		return this;
	}

}
