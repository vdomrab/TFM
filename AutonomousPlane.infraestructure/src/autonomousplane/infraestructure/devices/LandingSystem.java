package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.Thing;
import autonomousplane.interfaces.EFlyingStages;

public class LandingSystem  extends Thing implements ILandingSystem {

	public static final String RUNWAY_HEADING_DEGREES  = "runway_heading_degrees";

	public static final double ALIGNMENT_TOLERANCE_DEGREES = 10.0; // Allowable deviation

	public LandingSystem(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(LandingSystem.class.getName());
		this.setRunwayHeadingDegrees(0.0); // Default heading
}

	
	@Override
	public double getRunwayHeadingDegrees() {
		return (double) this.getProperty(RUNWAY_HEADING_DEGREES);
	}
	public ILandingSystem setRunwayHeadingDegrees(double getAngleDegrees) {
		if (getAngleDegrees < 0 || getAngleDegrees >= 360) {
			throw new IllegalArgumentException("Heading must be between 0 and 360 degrees.");
		}
		this.setProperty(RUNWAY_HEADING_DEGREES, getAngleDegrees);
		return this;
	}
	@Override
	public boolean isAlignedWithRunway(double yaw) {
	    double runwayHeading = this.getRunwayHeadingDegrees(); // Already in [0°, 360°]
	    double difference = Math.abs(yaw - runwayHeading);

	    if (difference > 180.0) {
	        difference = 360.0 - difference; // Normalize difference to [0°, 180°]
	    }

	    return difference <= ALIGNMENT_TOLERANCE_DEGREES;
	}
	
	
}
