package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.infraestructure.Thing;

public class ProximitySensor extends Thing implements IProximitySensor {

	public static final String CLOSE_OBJECT = "close_object";
	public ProximitySensor(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IProximitySensor.class.getName());
		this.setObjectDetected(false); // Inicialmente no hay objeto cercano
	}
	
	@Override
	public IProximitySensor setObjectDetected(boolean close) {
		this.setProperty(CLOSE_OBJECT, close);
		return this;
	}
	@Override
	public boolean isObjectDetected() {
		return (boolean) this.getProperty(CLOSE_OBJECT);
	}

}
