package autonomousplane.infraestructure.autopilot;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IL1_FlyingService;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;

public abstract class L1_FlyingService extends L0_FlyingService implements IL1_FlyingService {
	protected IControlSurfaces controlsurfaces = null;
	protected IAttitudeSensor AHRSSensor = null;
	public final static String STABILITY_MODE_ACTIVE = "stability-mode-active"; // boolean
	public L1_FlyingService(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IL1_FlyingService.class.getName());
	}

	
	@Override
	public void setStabilityModeActive(boolean isActive) {
		System.out.println("Setting stability mode active: " + isActive);
		this.setProperty(L1_FlyingService.STABILITY_MODE_ACTIVE, isActive);
		return;
	}
	
	public boolean getStabilityModeActive() {
		return (boolean)this.getProperty(L1_FlyingService.STABILITY_MODE_ACTIVE);
	}
	
	@Override
	public void setControlSurfaces(IControlSurfaces controlSurfaces) {
	
		this.controlsurfaces = controlSurfaces;
		return;
	}

	@Override
	public void setAHRSSensor(IAttitudeSensor sensor) {
		this.AHRSSensor = sensor;
		return;
	}
	public IAttitudeSensor getAHRSSensor() {
		return this.AHRSSensor;
	}
	
	public IControlSurfaces getControlSurfaces() {
		return this.controlsurfaces;
	}
	@Override
	protected boolean checkRequirementsToPerformTheFlyingService() {
		boolean result = true;
		if(this.getAHRSSensor() == null) {
			
			result = false;
			logger.warn("The AHRS sensor is not set. The flying service cannot be performed.");
		}
		if(this.getControlSurfaces() == null) {
			result = false;
			logger.warn("The ControlSurfaces system is not set. The flying service cannot be performed.");
		}
		
		return result && super.checkRequirementsToPerformTheFlyingService();
	}

}
