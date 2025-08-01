package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IThing;
import autonomousplane.infraestructure.Thing;
import autonomousplane.infraestructure.autopilot.AbstractAltitudeRateListener;

public class AltitudeSensor extends Thing implements IAltitudeSensor {

	public static final String ALTITUDE = "altitude";
	public static final String VERTICAL_ACCELERATION = "vertical_acceleration"; // Altitude increase in meters per second
	public static final String VERTICAL_SPEED = "vertical_speed"; // Altitude increase in meters per second
	
	public static final double MAX_ALTITUDE = 13000.0; // Max altitude in meters
	public static final double MIN_ALTITUDE = 0; // Max altitude in meters
	public static final double MAX_VERTICAL_ACCELERATION = 3.0;  // ≈ +2.5 g
	public static final double MIN_VERTICAL_ACCELERATION = -3.0; // ≈ -1.5 g
    private static final double PLANE_MASS = 70000 ; // Masa del avión en kg, un valor típico para un avión pequeño
	public static final double MAX_THRUST_FORCE = 243000; // Maximum thru st force in Newtons
	private final double GRAVITY = 9.81; // m/s2
	public static final double WING_AREA = 124.6; // m"2
	public static final double MAX_VERTICAL_SPEED = 12; // Maximum vertical speed in m/s
	public static final double MIN_VERTICAL_SPEED = -5; // Minimum vertical speed in m/s

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
	
	public double calculateVerticalAcceleration(
		    double thrust,            // porcentaje de thrust (0–100)
		    double pitchDegrees,      // ángulo de cabeceo
		    double airDensity,        // ρ en kg/m³ (ISA)
		    double speed,             // Velocidad total del avión (TAS)
		    double aoa               // Ángulo de ataque en grados
		) {
		    double pitchRad = Math.toRadians(pitchDegrees);
		    double aoaRad = Math.toRadians(aoa);
		    double thrustN = (thrust / 100.0) * MAX_THRUST_FORCE;

		    // === COMPONENTE VERTICAL DEL EMPUJE ===
		    double FThrustVertical = thrustN * Math.sin(pitchRad);

		    // === LIFT PERPENDICULAR AL FLUJO ===
		    double lift = 0.5 * airDensity * Math.pow(speed, 2) * calculateLiftCoefficient(aoaRad) * WING_AREA;

		    // === PROYECTAR LIFT EN VERTICAL (respecto al suelo) ===
		    // Lift actúa perpendicular al flujo, así que en vertical es:
		    double FLiftVertical = lift * Math.cos(aoaRad);

		    // === DRAG COMPLETO (opuesto al flujo) ===
		    double dragCoefficient = 0.03;
		    double drag = 0.5 * airDensity * Math.pow(speed, 2) * dragCoefficient * WING_AREA;

		    // Proyección vertical del drag (opuesto a trayectoria, no solo pitch)
		    double FDragVertical = drag * Math.sin(pitchRad);  // Aprox, si no calculás flight path angle directamente

		    // === PESO DEL AVIÓN ===
		    double weight = PLANE_MASS * 9.81;

		    // === FUERZA NETA EN VERTICAL ===
		    double netVerticalForce = FThrustVertical + FLiftVertical - FDragVertical - weight;

		    // === ACELERACIÓN VERTICAL ===
		    double verticalAcceleration = netVerticalForce / PLANE_MASS;

		    // === LIMITAR A RANGOS FÍSICOS REALISTAS ===
		    return Math.max(MIN_VERTICAL_ACCELERATION, Math.min(verticalAcceleration, MAX_VERTICAL_ACCELERATION));
		}
	
	protected double calculateLiftCoefficient(double aoaRad) {
	    final double aoaCriticalRad = Math.toRadians(13.0); // Stall típico
	    final double stallDropRange = Math.toRadians(3.0);  // Caída agresiva en 3°
	    final double CL_0 = 0.4;
	    final double CL_alpha = 5.7;
	    final double CL_max = 1.5;

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
