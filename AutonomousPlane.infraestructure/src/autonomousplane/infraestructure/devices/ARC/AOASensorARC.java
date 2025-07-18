package autonomousplane.infraestructure.devices.ARC;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.impl.AdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.AOASensor;
import autonomousplane.devices.interfaces.IAOASensor;

public class AOASensorARC extends AdaptiveReadyComponent implements IAdaptiveReadyComponent {
	public static String PROVIDED_DEVICE = "provided_device";
	protected IAOASensor device = null;
	
	public AOASensorARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.device = new AOASensor(this.context, id);
	}
	
	@Override
	public IAdaptiveReadyComponent deploy() {
		((AOASensor)this.device).registerThing();
		return super.deploy();
	}
	@Override
	public IAdaptiveReadyComponent undeploy() {
		((AOASensor)this.device).unregisterThing();
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
