package autonomousplane.infraestructure.autopilotARC;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL0_FlyingService;
import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.interaction.interfaces.INotificationService;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;


public class L0_FlyingServiceARC extends FlyingServiceARC {


	
	public L0_FlyingServiceARC(BundleContext context, String bundleId) {
		super(context, bundleId);
	}

	protected IL0_FlyingService getTheL0FlyingService() {
		return (IL0_FlyingService) this.getTheFlyingService();
	}

	
	
	
}
