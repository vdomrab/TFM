package autonomousplane.infraestructure.devices.ARC;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.infraestructure.devices.EGTSensor;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.impl.AdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class EGTSensorARC extends AdaptiveReadyComponent implements IAdaptiveReadyComponent {

	public static String PROVIDED_SENSOR = "provided_sensor";
	protected IEGTSensor sensor = null;

	public EGTSensorARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.sensor = new EGTSensor(context, id);
	}

	@Override
	public IAdaptiveReadyComponent deploy() {
	   
	        ((EGTSensor)this.sensor).registerThing();
	        System.out.println("Deploying EGTS with id: " + this.getId());
	   
	    return super.deploy();
	}


	@Override
	public IAdaptiveReadyComponent undeploy() {
		((EGTSensor) this.sensor).unregisterThing();
		this.sensor = null;
		return super.undeploy();
	}

	@Override
	public Object getServiceSupply(String serviceSupply) {
		if (serviceSupply.equals(PROVIDED_SENSOR)) {
			super.getServiceSupply(serviceSupply);
			return this.sensor;
		}
		
		return super.getServiceSupply(serviceSupply);
	}
}
