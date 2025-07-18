package autonomousplane.infraestructure.devices.ARC;

import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.impl.AdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.devices.AltitudeSensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;

public class AltitudeSensorARC extends AdaptiveReadyComponent implements IAdaptiveReadyComponent {
	public static String PROVIDED_DEVICE = "provided_device";
	protected IAltitudeSensor device = null;
	
	public AltitudeSensorARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.device = new AltitudeSensor(this.context, id);
	}
	
	@Override
	public IAdaptiveReadyComponent deploy() {
		((AltitudeSensor)this.device).registerThing();
		return super.deploy();
	}
	
	@Override
	public IAdaptiveReadyComponent undeploy() {
		((AltitudeSensor)this.device).unregisterThing();
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
