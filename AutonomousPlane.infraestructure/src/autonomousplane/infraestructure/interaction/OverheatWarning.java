package autonomousplane.infraestructure.interaction;

import org.osgi.framework.BundleContext;

public class OverheatWarning extends InteractionMechanism {

	public OverheatWarning(BundleContext context, String device)  {
		super(context, String.format("%s", device));
	}

	@Override
	public OverheatWarning performTheInteraction(String message) {
		this.showMessage("🔥 " + message);
		return this;
	}

}
