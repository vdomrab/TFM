package autonomousplane.infraestructure.autopilotARC;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.infraestructure.Thing;
import autonomousplane.autopilot.interfaces.IL0_FlyingService;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.impl.AdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;


public class FlyingServiceARC extends AdaptiveReadyComponent implements IAdaptiveReadyComponent {
	public static String PROVIDED_FLYINGSERVICE = "provided_flyingservice";
	protected IFlyingService theFlyingService = null;

	public FlyingServiceARC(BundleContext context, String bundleId) {
		super(context, bundleId);
	}
	
	protected FlyingServiceARC setTheFlyingService(IFlyingService theDrivingService) {
		this.theFlyingService = theDrivingService;
		return this;
	}
	
	protected IFlyingService getTheFlyingService() {
		return theFlyingService;
	}
		
	@Override
	public IAdaptiveReadyComponent deploy() {
		
		((Thing) this.getTheFlyingService()).registerThing();
		this.getTheFlyingService().startFlight();
		return super.deploy();
	}
	
	
	@Override
	public IAdaptiveReadyComponent undeploy() {
		this.getTheFlyingService().endFlight();
		((Thing) this.getTheFlyingService()).unregisterThing();
		return super.undeploy();
	}
	
	@Override
	public Object getServiceSupply(String serviceSupply) {

		if (serviceSupply.equals(PROVIDED_FLYINGSERVICE)) {
			super.getServiceSupply(serviceSupply);
			return (IL0_FlyingService) this.getTheFlyingService();
		}
		
		return super.getServiceSupply(serviceSupply);
	}

}
