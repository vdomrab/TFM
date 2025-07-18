package autonomousplane.infraestructure.autopilotARC;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFallbackPlan;
import autonomousplane.infraestructure.Thing;
import autonomousplane.interaction.interfaces.INotificationService;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;


public abstract class FallbackPlanARC extends FlyingServiceARC {
	public static String REQUIRED_NOTIFICATIONSERVICE = "required_notificationservice";

	public FallbackPlanARC(BundleContext context, String bundleId) {
		super(context, bundleId);
	}
	
	@Override
	public IAdaptiveReadyComponent deploy() {
		((Thing) this.getTheFlyingService()).registerThing();
		return this;
	}
	
	protected IFallbackPlan getTheFallbackPlanFlyingService() {
		return (IFallbackPlan) this.getTheFlyingService();
	}
	
	@Override
	public IAdaptiveReadyComponent bindService(String req, Object value) {
		if (req.equals(REQUIRED_NOTIFICATIONSERVICE))
			this.getTheFallbackPlanFlyingService().setNotificationService((INotificationService) value);
		return super.bindService(req, value);
	}

	@Override
	public IAdaptiveReadyComponent unbindService(String req, Object value) {
		
		if (req.equals(REQUIRED_NOTIFICATIONSERVICE))
			this.getTheFallbackPlanFlyingService().setNotificationService(null);
		return super.unbindService(req, value);
	}
	
}
