package autonomousplane.infraestructure.devices.ARC;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.infraestructure.devices.FuelSensor;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.impl.AdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class FuelSensorARC extends AdaptiveReadyComponent implements IAdaptiveReadyComponent {

	public static String PROVIDED_SENSOR = "provided_sensor";
	protected IFuelSensor sensor = null;

	public FuelSensorARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.sensor = new FuelSensor(this.context, id);
	}

	@Override
	public IAdaptiveReadyComponent deploy() {
		((FuelSensor)this.sensor).registerThing();
		return super.deploy();
	}

	@Override
	public IAdaptiveReadyComponent undeploy() {
		((FuelSensor)this.sensor).unregisterThing();
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
