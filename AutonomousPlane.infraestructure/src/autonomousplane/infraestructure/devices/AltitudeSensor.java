package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IThing;
import autonomousplane.infraestructure.Thing;
import autonomousplane.infraestructure.autopilot.AbstractAltitudeRateListener;

public class AltitudeSensor extends Thing implements IAltitudeSensor {

	public static final String ALTITUDE = "altitude";
	public static final double MAX_ALTITUDE = 13000.0; // Max altitude in meters
	public static final double MIN_ALTITUDE = 0; // Max altitude in meters
	public static final String VERTICAL_ACCELERATION = "vertical_acceleration"; // Altitude increase in meters per second
	public static final String VERTICAL_SPEED = "vertical_speed"; // Altitude increase in meters per second
	
	public static final double MAX_VERTICAL_ACCELERATION = 10.0;  // ≈ +2.5 g
	public static final double MIN_VERTICAL_ACCELERATION = -8.0; // ≈ -1.5 g
    private static final double PLANE_MASS = 70000 ; // Masa del avión en kg, un valor típico para un avión pequeño
	public static final double MAX_THRUST_FORCE = 243000; // Maximum thrust force in Newtons
	public static final double WING_AREA = 124.6; // m"2
	public static final double MAX_VERTICAL_SPEED = 5.0; // Maximum vertical speed in m/s
	public static final double MIN_VERTICAL_SPEED = -15.0; // Minimum vertical speed in m/s
	
	//protected AltitudeRateListener listener = null;
	
	public AltitudeSensor(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IAltitudeSensor.class.getName());
		this.setAltitude(0.0);
		this.setVerticalAcceleration(0.0);
		this.setVerticalSpeed(0);// Initialize altitude rate to 0 m/s
		//this.listener = new AltitudeRateListener(context, this);
	}

	@Override
	public double getAltitude() {
	    Double value = (Double) this.getProperty(AltitudeSensor.ALTITUDE);
	    return (value != null) ? value.doubleValue() : 0.0;
	}

	public double getVerticalAcceleration() {
	    Double value = (Double) this.getProperty(AltitudeSensor.VERTICAL_ACCELERATION);
	    return (value != null) ? value.doubleValue() : 0.0;
	}
	public double getVerticalSpeed() {
	    Double value = (Double) this.getProperty(AltitudeSensor.VERTICAL_SPEED);
	    return (value != null) ? value.doubleValue() : 0.0;
	}
	
	@Override
	public IAltitudeSensor setAltitude(double altitude) {
		double clampedAltitude = Math.max(MIN_ALTITUDE, Math.min(altitude, AltitudeSensor.MAX_ALTITUDE));
		if(altitude == AltitudeSensor.MAX_ALTITUDE && this.getVerticalAcceleration() > 0) {
			this.setVerticalAcceleration(0);
			this.setVerticalSpeed(0);// Prevent increasing altitude beyond max limit
		}else if(altitude == AltitudeSensor.MIN_ALTITUDE && this.getVerticalAcceleration() < 0) {
			this.setVerticalAcceleration(0);// Prevent increasing altitude beyond max limit
			this.setVerticalSpeed(0);// Prevent decreasing altitude below min limit
		}
		this.setProperty(AltitudeSensor.ALTITUDE, clampedAltitude);
		return this;
	}
	
	@Override
	public IAltitudeSensor setVerticalAcceleration(double verticalAcceleration) {
		double clampedAltitudeRate = Math.max(AltitudeSensor.MIN_VERTICAL_ACCELERATION, Math.min(verticalAcceleration, AltitudeSensor.MAX_VERTICAL_ACCELERATION));
		
		this.setProperty(AltitudeSensor.VERTICAL_ACCELERATION, clampedAltitudeRate);
		return this;
	}
	public IAltitudeSensor setVerticalSpeed(double verticalSpeed) {
		double clampedVerticalSpeed = Math.max(AltitudeSensor.MIN_VERTICAL_SPEED, Math.min(verticalSpeed, AltitudeSensor.MAX_VERTICAL_SPEED));
		this.setProperty(AltitudeSensor.VERTICAL_SPEED, clampedVerticalSpeed);
		return this;
	}
	public double calculateAltitudeRATE(double speed, double pitchAngle) {
		double speedInMetersPerSecond = speed; // Convert km/h to m/s
		double pitchRad = Math.toRadians(pitchAngle);
		double altitudeRate = speedInMetersPerSecond * Math.sin(pitchRad);
		
		return altitudeRate;
	}
	// Java
	public double calculateVerticalAcceleration(
	    double thrust,            // porcentaje de thrust (0–100)
	    double pitchDegrees,      // ángulo de cabeceo
	    double airDensity,        // ρ en kg/m³ (ISA)
	    double speed,
	    double aoa
	) {
	    double horizontalSpeed = speed; // Only horizontal speed generates lift
	    double pitchRad = Math.toRadians(pitchDegrees);
	    double thrustN = (thrust / 100.0) * MAX_THRUST_FORCE;

	    double FThrustVertical = thrustN * Math.sin(pitchRad);

	    // More physical lift calculation
	    double lift = 0.5 * airDensity * Math.pow(horizontalSpeed, 2) * calculateLiftCoefficient(Math.toRadians(aoa)) * WING_AREA;
	    double FLiftVertical = lift;

	    // Add drag (vertical component)
	    double dragCoefficient = 0.03; // Typical for jet airliners
	    double drag = 0.5 * airDensity * Math.pow(horizontalSpeed, 2) * dragCoefficient * WING_AREA;
	    double FDragVertical = drag * Math.sin(pitchRad);

	    double weight = PLANE_MASS * 9.81;

	    double netVerticalForce = FThrustVertical + FLiftVertical - FDragVertical - weight;
	    double verticalAcceleration = netVerticalForce / PLANE_MASS;
	    verticalAcceleration = verticalAcceleration * 0.5;
	    // Clamp to realistic values
	    return Math.max(MIN_VERTICAL_ACCELERATION, Math.min(verticalAcceleration, MAX_VERTICAL_ACCELERATION));
	}
	
	protected double calculateLiftCoefficient(double aoaRad) {
	    final double aoaCriticalRad = Math.toRadians(15.0); // Stall típico
	    final double stallDropRange = Math.toRadians(3.0);  // Caída agresiva en 3°
	    final double CL_0 = 0.2;
	    final double CL_alpha = 5.7;
	    final double CL_max = 1.2;

	    double aoaAbs = Math.abs(aoaRad);
	    double sign = Math.signum(aoaRad);

	    if (aoaAbs <= aoaCriticalRad) {
	        double cl = CL_0 + CL_alpha * aoaRad;
	        return Math.min(cl, CL_max);
	    } else {
	        double clCritical = Math.min(CL_0 + CL_alpha * aoaCriticalRad, CL_max);
	        double excessAOA = aoaAbs - aoaCriticalRad;

	        // Make the drop more aggressive
	        double dropFactor = Math.exp(-30 * (excessAOA / stallDropRange));

	        return clCritical * dropFactor * sign;
	    }
	
	}



	
	/*
	 * public double calculateVerticalAcceleration(
    double thrust, // Empuje en Newtons
    double speed,  // Velocidad en km/h
    double pitchAngle, // en grados
    double airDensity, // densidad en kg/m^3
    double dragCoefficient,
    double frontalArea,
    double mass
) {
    double g = 9.81; // gravedad m/s^2
    double speedMS = speed / 3.6; // convertir km/h a m/s
    double pitchRad = Math.toRadians(pitchAngle);

    // Calcular drag
    double drag = 0.5 * airDensity * speedMS * speedMS * dragCoefficient * frontalArea;

    // Fuerza vertical neta (proyectando empuje y drag en vertical)
    double verticalForce = thrust * Math.sin(pitchRad) - drag * Math.sin(pitchRad) - mass * g;

    // Aceleración vertical = fuerza neta / masa
    double verticalAcceleration = verticalForce / mass;

    return verticalAcceleration; // en m/s^2
}

	 */
	
	/*/ @Override
		public IThing registerThing() {
			super.registerThing();
			this.listener.start();
			return this;
		}
		
	@Override
	public IThing unregisterThing() {
			this.listener.stop();	this.listener = null;
			super.unregisterThing();
			return this;
	}*/
	
	public class AltitudeRateListener extends AbstractAltitudeRateListener {
        private AltitudeSensor altitudeSensor;

        public AltitudeRateListener(BundleContext context, AltitudeSensor altitudeSensor) {
            super(context);
            this.altitudeSensor = altitudeSensor;
        }

        @Override
        protected double calculateAltitudeRATE(double speed, double pitchAngle) {
            return altitudeSensor.calculateAltitudeRATE(speed, pitchAngle);
        }

        @Override
        protected void setAltitudeRates(double altitudeRate) {
            altitudeSensor.setVerticalAcceleration(altitudeRate);
        }
        
    }
		
	
}
