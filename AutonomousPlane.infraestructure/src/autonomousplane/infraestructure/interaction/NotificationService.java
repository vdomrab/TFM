package autonomousplane.infraestructure.interaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleContext;

import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.Thing;
import autonomousplane.interaction.interfaces.IInteractionMechanism;
import autonomousplane.interaction.interfaces.INotificationService;
import autonomousplane.interfaces.IIdentifiable;


public class NotificationService extends Thing implements INotificationService {

	protected List<String> mechanisms = null;
	
	public NotificationService(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(INotificationService.class.getName());
		this.mechanisms = new ArrayList<String>();
	}
	@Override
	public INotificationService notify(String message, String... mechanismsNames) {
	    if (mechanisms == null || mechanisms.isEmpty())
	        return this;

	    // Convertimos la lista de mecanismos permitidos a un Set para búsqueda rápida
	    Set<String> allowedMechanisms = new HashSet<>(Arrays.asList(mechanismsNames));

	    for (String m : this.mechanisms) {
	        // Solo enviamos si el mecanismo actual está en los permitidos
	        if (!allowedMechanisms.contains(m)) continue;

	        IInteractionMechanism mechanism = OSGiUtils.getService(
	            context,
	            IInteractionMechanism.class,
	            String.format("(%s=%s)", IIdentifiable.ID, m)
	        );

	        if (mechanism != null) {
	            mechanism.performTheInteraction(message);
	        }
	    }

	    return this;
	}

	@Override
	public INotificationService addInteractionMechanism(String m) {
		this.mechanisms.add(m);
		return this;
	}
	
	@Override
	public boolean isMechanismAvailable(String id) {
	    IInteractionMechanism mech = OSGiUtils.getService(
	        context,
	        IInteractionMechanism.class,
	        String.format("(%s=%s)", IIdentifiable.ID, id)
	    );
	    return mech != null;
	}
	
	@Override
	public INotificationService removeInteractionMechanism(String m) {
		this.mechanisms.remove(m);
		return this;
	}

}
