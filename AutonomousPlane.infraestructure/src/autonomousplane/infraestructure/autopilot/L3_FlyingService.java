package autonomousplane.infraestructure.autopilot;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.autopilot.interfaces.IL3_FlyingService;
import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.devices.interfaces.IETL;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.IProximitySensor;

public abstract class L3_FlyingService extends L2_FlyingService implements IL3_FlyingService {
	protected IProximitySensor proximitySensor = null;
	protected IEGTSensor egtSensor = null; 
	protected ILandingSystem landingSystem = null;
	protected IFuelSensor fuelSensor = null;
	
	public L3_FlyingService(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IL3_FlyingService.class.getName());
	}
	@Override
	public void setEGTSensor(IEGTSensor sensor) {
		this.egtSensor = sensor;
		return;
	}
	@Override
	public void setFuelSensor(IFuelSensor sensor) {
		this.fuelSensor = sensor;
		return;
	}
	@Override
	public void setProximitySensor(IProximitySensor sensor) {
		this.proximitySensor = sensor;
		return;
	}
	@Override
	public void setLandingSystem(ILandingSystem landingSystem) {
		this.landingSystem = landingSystem;
		return;
	}
	
	public IEGTSensor getEGTSensor() {
		return this.egtSensor;
	}
	public IFuelSensor getFuelSensor() {
		return this.fuelSensor;
	}
	
	public IProximitySensor getProximitySensor() {
		return this.proximitySensor;
	}
	public ILandingSystem getLandingSystem() {
		return this.landingSystem;
	}
	
	
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean result = true;
		
		if (this.egtSensor == null) {
			result = false;
			logger.warn("EGTSensor not set");
		} else if (this.fuelSensor == null) {
			result = false;
			logger.warn("Fuel Sensor not set");
		} else if (this.proximitySensor == null) {
			result = false;
			logger.warn("Proximity Sensor not set");
		} else if (this.landingSystem == null) {
			result = false;
			logger.warn("Landing System not set");
		}
		return result && super.checkRequirementsToPerformTheFlyingService();
	}

	

}
