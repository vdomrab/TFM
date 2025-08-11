package autonomousplane.infraestructure.listeners;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.infraestructure.devices.AltitudeSensor;

public class SpeedChangeListener implements ServiceListener {
	protected BundleContext context = null;
	protected AltitudeSensor speedodometer = null;

	public SpeedChangeListener(BundleContext context, AltitudeSensor speedometer) {
		this.context = context;
		this.speedodometer = speedometer;
	}
	
	public void start() {
		String filter = "(" + Constants.OBJECTCLASS + "=" + IFADEC.class.getName() + ")";
		try {
			this.context.addServiceListener(this, filter);
		} catch (InvalidSyntaxException e) {
		}
	}
	public void stop() {
		this.context.removeServiceListener(this);
	}
	@Override
	public void serviceChanged(ServiceEvent event) {
	
		//IFADEC FADEC = (IFADEC)context.getService(event.getServiceReference());
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
		case ServiceEvent.MODIFIED:
			//this.speedodometer.calculateSpeedFromThrust(FADEC.getCurrentThrust());
			break;

		case ServiceEvent.UNREGISTERING:
		case ServiceEvent.MODIFIED_ENDMATCH:
			//this.speedodometer.calculateSpeedFromThrust(0);
			break;
		default:
			break;
		}
	}

}
