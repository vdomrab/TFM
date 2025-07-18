package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.infraestructure.Thing;

public class FADEC extends Thing implements IFADEC{
	public final static String THRUST = "THRUST";
	public final static double MIN_THRUST = 0;     
	public final static double MAX_THRUST = 100;   

	// Aceleraciones típicas
	public final static double LIGHT_ACCELERATION = 5;    
	public final static double MEDIUM_ACCELERATION = 20;  
	public final static double AGGRESSIVE_ACCELERATION = 40; 
	
	
	public FADEC(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IFADEC.class.getName());
		this.setTHRUSTPercentage(FADEC.MIN_THRUST);
	}
	

	@Override
	public IFADEC setTHRUSTPercentage(double thrust) {
		this.setProperty(FADEC.THRUST, clamp(thrust, FADEC.MIN_THRUST, FADEC.MAX_THRUST));
		return null;
	}

	@Override
	public double getCurrentThrust() {
		return (double)this.getProperty(FADEC.THRUST);
	}
	
	public double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

}
