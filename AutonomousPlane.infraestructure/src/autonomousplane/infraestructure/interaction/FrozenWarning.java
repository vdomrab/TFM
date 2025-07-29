package autonomousplane.infraestructure.interaction;

import org.osgi.framework.BundleContext;

public class FrozenWarning extends InteractionMechanism {

	public FrozenWarning(BundleContext context, String device)  {
		super(context, String.format("%s", device));
	}

	@Override
	public FrozenWarning performTheInteraction(String message) {
		System.out.println("Frozen Warning ❄️ - " + message);
		return this;
	}

}
