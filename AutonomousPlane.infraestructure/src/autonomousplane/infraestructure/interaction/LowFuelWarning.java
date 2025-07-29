package autonomousplane.infraestructure.interaction;

import org.osgi.framework.BundleContext;

public class LowFuelWarning extends InteractionMechanism {

	public LowFuelWarning(BundleContext context, String device)  {
		super(context, String.format("%s", device));
	}

	@Override
	public LowFuelWarning performTheInteraction(String message) {
		System.out.println("¨LowFuel Warning ⛽ - " + message);
		return this;
	}
}
