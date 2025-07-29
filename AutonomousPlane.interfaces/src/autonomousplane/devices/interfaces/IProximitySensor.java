package autonomousplane.devices.interfaces;

public interface IProximitySensor {
	boolean isObjectDetected();
	
	IProximitySensor setObjectDetected(boolean detected);
}
