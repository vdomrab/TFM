package autonomousplane.infraestructure.autopilot;

import org.osgi.framework.*;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
public abstract class AbstractAltitudeRateListener implements ServiceListener {

	    protected BundleContext context;

	    public AbstractAltitudeRateListener(BundleContext context) {
	        this.context = context;
	    }

	    public void start() {
	        String filterSpeed = "(" + Constants.OBJECTCLASS + "=" + ISpeedSensor.class.getName() + ")";
	        String filterAngle = "(" + Constants.OBJECTCLASS + "=" + IAttitudeSensor.class.getName() + ")";
	        try {
	            this.context.addServiceListener(this, filterSpeed);
	            this.context.addServiceListener(this, filterAngle);
	        } catch (InvalidSyntaxException e) {
	            e.printStackTrace();
	        }
	    }

	    public void stop() {
	        this.context.removeServiceListener(this);
	    }

	    @Override
	    public void serviceChanged(ServiceEvent event) {
	        ISpeedSensor speedSensor = null;
	        IAttitudeSensor attitudeSensor = null;

	        try {
	            ServiceReference<?>[] speedRefs = context.getServiceReferences(ISpeedSensor.class.getName(), null);
	            if (speedRefs != null && speedRefs.length > 0) {
	                speedSensor = (ISpeedSensor) context.getService(speedRefs[0]);
	            }
	        } catch (InvalidSyntaxException e) {
	            e.printStackTrace();
	        }

	        try {
	            ServiceReference<?>[] attitudeRefs = context.getServiceReferences(IAttitudeSensor.class.getName(), null);
	            if (attitudeRefs != null && attitudeRefs.length > 0) {
	                attitudeSensor = (IAttitudeSensor) context.getService(attitudeRefs[0]);
	            }
	        } catch (InvalidSyntaxException e) {
	            e.printStackTrace();
	        }

	        switch (event.getType()) {
	            case ServiceEvent.REGISTERED:
	            case ServiceEvent.MODIFIED:
	                if (speedSensor != null && attitudeSensor != null) {
	                    double speed = speedSensor.getSpeedTAS();
	                    double pitch = attitudeSensor.getPitch();
	                    double altitudeRate = calculateAltitudeRATE(speed, pitch);
	                    setAltitudeRates(altitudeRate);
	                }
	                break;
	            case ServiceEvent.UNREGISTERING:
	            case ServiceEvent.MODIFIED_ENDMATCH:
	                // Optional: handle sensor removal if needed
	                break;
	            default:
	                break;
	        }
	    }

	    protected abstract double calculateAltitudeRATE(double speed, double pitchAngle);
	    protected abstract void setAltitudeRates(double altitudeRate);
}

