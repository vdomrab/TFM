package autonomousplane.infraestructure.interaction;

import org.osgi.framework.BundleContext;

public class OverheatWarning extends InteractionMechanism {

	public OverheatWarning(BundleContext context, String device)  {
		super(context, String.format("%s", device));
	}

	@Override
	public OverheatWarning performTheInteraction(String message) {
		System.out.println("Overheat Warning 🔥 - " + message);
		return this;
	}

}
