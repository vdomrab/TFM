package autonomousplane.infraestructure.devices.ARC;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.infraestructure.devices.NavigationSystem;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.impl.AdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class NavigationSystemARC extends AdaptiveReadyComponent implements IAdaptiveReadyComponent {

	public static String PROVIDED_DEVICE = "provided_device";
	protected INavigationSystem device = null;
	
	public NavigationSystemARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.device = new NavigationSystem(context, id);

	}
	@Override
	public IAdaptiveReadyComponent deploy() {
		System.out.println("Deploying GNSSARC with id: " + this.getId());
		((NavigationSystem)this.device).registerThing();
		return super.deploy();
	}

	@Override
	public IAdaptiveReadyComponent undeploy() {
		((NavigationSystem)this.device).unregisterThing();
		this.device = null;
		return super.undeploy();
	}

	@Override
	public Object getServiceSupply(String serviceSupply) {
		if (serviceSupply.equals(PROVIDED_DEVICE)) {
			super.getServiceSupply(serviceSupply);
			return this.device;
		}
		
		return super.getServiceSupply(serviceSupply);
	}

}
