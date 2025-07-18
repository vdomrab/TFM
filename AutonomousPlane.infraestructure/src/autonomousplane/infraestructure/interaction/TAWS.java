package autonomousplane.infraestructure.interaction;

import org.osgi.framework.BundleContext;

public class TAWS extends InteractionMechanism {

	public TAWS(BundleContext context, String device) {
		super(context, String.format("%s", device));
	}

	@Override
	public TAWS performTheInteraction(String message) {
		System.out.println("TAWS- " + message);
		return this;
	}

}
