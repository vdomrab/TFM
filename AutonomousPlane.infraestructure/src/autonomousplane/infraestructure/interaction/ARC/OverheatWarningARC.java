package autonomousplane.infraestructure.interaction.ARC;

import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.interaction.OverheatWarning;
import autonomousplane.infraestructure.interaction.StallWarning;
import autonomousplane.interaction.interfaces.IInteractionMechanism;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.impl.AdaptiveReadyComponent;
import es.upv.pros.tatami.adaptation.mapek.lite.ARC.artifacts.interfaces.IAdaptiveReadyComponent;
import es.upv.pros.tatami.osgi.utils.logger.SmartLogger;

public class OverheatWarningARC extends AdaptiveReadyComponent implements IAdaptiveReadyComponent {
	
	public static String PROVIDED_MECHANISM = "provided_mechanism";
	protected IInteractionMechanism mechanism = null;
	
	public OverheatWarningARC(BundleContext context, String id) {
		super(context, context.getBundle().getSymbolicName());
		logger = SmartLogger.getLogger(context.getBundle().getSymbolicName());
		this.mechanism = new OverheatWarning(this.context, id);
	}
	
	@Override
	public IAdaptiveReadyComponent deploy() {
		((OverheatWarning)this.mechanism).registerThing();
		return super.deploy();
	}
	
	@Override
	public IAdaptiveReadyComponent undeploy() {
		((OverheatWarning)this.mechanism).unregisterThing();
		this.mechanism = null;
		return super.undeploy();
	}
	
	@Override
	public Object getServiceSupply(String serviceSupply) {
		if (serviceSupply.equals(PROVIDED_MECHANISM)) {
			super.getServiceSupply(serviceSupply);
			return this.mechanism;
		}
		
		return super.getServiceSupply(serviceSupply);
	}

}
