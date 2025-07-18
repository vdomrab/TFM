package autonomousplane.infraestructure.autopilot;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.infraestructure.Thing;
import autonomousplane.simulation.interfaces.ISimulationElement;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public abstract class FlyingService extends Thing implements IFlyingService, ISimulationElement {

	protected SmartLogger logger = SmartLogger.getLogger(FlyingService.class);
	public static final String ACTIVE = "active";

	public FlyingService(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IFlyingService.class.getName());
		this.addImplementedInterface(ISimulationElement.class.getName());
		this.setProperty(FlyingService.ACTIVE, false);
	}

	@Override
	public IFlyingService startFlight() {
		if (this.isFlying())
			return this;

		logger.debug("Starting the flying function ...");
		this.setProperty(FlyingService.ACTIVE, true);
		this.fly();
		return this;
	}

	@Override
	public IFlyingService endFlight() {
		if (!this.isFlying())
			return this;

		logger.debug("Ending the flying function ...");
		this.setProperty(FlyingService.ACTIVE, false);
		this.stopTheFlyingFunction();
		return this;
	}

	@Override
	public boolean isFlying() {
		return (boolean) this.getProperty(FlyingService.ACTIVE);
	}

	protected void fly() {
		if (this.checkRequirementsToPerformTheFlyingService())
			this.performTheFlyingFunction();
	}

	public abstract IFlyingService performTheFlyingFunction();
	public abstract IFlyingService stopTheFlyingFunction();
	
	protected abstract boolean checkRequirementsToPerformTheFlyingService();

	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		if ( this.isFlying() && this.checkRequirementsToPerformTheFlyingService() )
				this.performTheFlyingFunction();
	}
}
