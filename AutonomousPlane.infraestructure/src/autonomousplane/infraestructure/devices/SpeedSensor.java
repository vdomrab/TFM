package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IThing;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.Thing;

public class SpeedSensor extends Thing implements ISpeedSensor {
	public static final String SPEED_TAS = "speedTAS";
	public static final String SPEED_GS = "speedGS";
	public static final String SPEED_INCREASE = "speed_increase";
	public static final double MAX_SPEED =250.0; // Maximum speed in m/s
	public static final double MIN_SPEED = 0.0; // Minimum speed in m/s
	public static final double MAX_THRUST_FORCE = 243000;
	public static final double DRAG_COEFFICIENT = 0.025; 
	public static final double FRONTAL_AREA = 80.0; // Área frontal en m², un valor típico para un avión pequeño
	public static final double PLANE_MASS = 79000 ; // Masa del avión en kg, un valor típico para un avión pequeño
    public static final double AIR_BRAKE_DRAG_COEFFICIENT = 0.9; // o un valor entre 0.3 y 0.6 según el modelo
    
	protected FADECListener listener = null;
	public SpeedSensor(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(ISpeedSensor.class.getName());
		this.setSpeedGS(0.0); // Initialize target speed to 0 km/h
		this.setSpeedTAS(0.0); // Initialize speed to 0 km/h
		this.setSpeedIncreaseTAS(0.0);
		this.listener = new FADECListener(context, this);
	}
	@Override
	public double getSpeedGS() {
		Object value = this.getProperty(SpeedSensor.SPEED_GS);
		if (value == null) {
			return 0.0; // o algún valor seguro por defecto
		}
		return (double) value;
	}
	@Override
	public double getSpeedIncreaseTAS() {
		return (double) this.getProperty(SpeedSensor.SPEED_INCREASE);
	}
	@Override
	public ISpeedSensor setSpeedTAS(double speed) {
		this.setProperty(SpeedSensor.SPEED_TAS, clampSpeed(speed));
		return this;
	}
	
	
	@Override
	public double getSpeedTAS() {
		return (double) this.getProperty(SpeedSensor.SPEED_TAS);
	}
	
   
    public ISpeedSensor setSpeedIncreaseTAS(double acceleration) {
    	this.setProperty(SPEED_INCREASE, acceleration);
        return this;
    }

    public ISpeedSensor setSpeedGS(double targetSpeed) {
		this.setProperty(SpeedSensor.SPEED_GS, Math.max(MIN_SPEED, targetSpeed));
		return this;
	}

    public double calcualteSpeedIncreaseTAS(double thrust, double airDensity, double airBrakeLevel, double pitchDegrees, boolean isOnGround, double altitude) {
    	    double currentTAS = this.getSpeedTAS(); // m/s
    	    double pitchRad = Math.toRadians(pitchDegrees);

    	    // === THRUST TOTAL CON EFICIENCIA POR ALTITUD ===
    	    double thrustTotal = (thrust / 100.0) * MAX_THRUST_FORCE;

    	    // Pérdida de empuje progresiva hasta un 30% entre 0–15.000 m
    	    double thrustEfficiency = 1.0 - Math.min(altitude / 15000.0, 0.3); 
    	    thrustTotal *= thrustEfficiency;

    	    // === THRUST HORIZONTAL ===
    	    double thrustHorizontal = thrustTotal * Math.cos(pitchRad); // reducción por inclinación

    	    // === DRAG AERODINÁMICO REALISTA ===
    	    double effectiveCd = DRAG_COEFFICIENT + (AIR_BRAKE_DRAG_COEFFICIENT * airBrakeLevel);
    	    double dragForce = 0.5 * effectiveCd * airDensity * SpeedSensor.FRONTAL_AREA * Math.pow(currentTAS, 2);

    	    // === FUERZAS SOBRE EL AVIÓN ===
    	    double netForce = thrustHorizontal - dragForce;

    	    if (isOnGround) {
    	        // RESISTENCIA POR RODADURA
    	        double rollingFrictionCoefficient = 0.03;
    	        double rollingResistance = rollingFrictionCoefficient * PLANE_MASS * 9.81;
    	        netForce -= rollingResistance;

    	        // FRENADO POR RUEDAS SI AIRBRAKE ALTO
    	        if (airBrakeLevel > 0.7) {
    	            double brakingDecelG = 0.25;
    	            double brakingForce = brakingDecelG * PLANE_MASS * 9.81;
    	            netForce -= brakingForce;
    	        }

    	        // EMPUJE REVERSO SI THRUST = 0
    	        if (thrust == 0) {
    	            double reverseThrust = 0.2 * MAX_THRUST_FORCE;
    	            netForce -= reverseThrust;
    	        }
    	    }

    	    // === ACELERACIÓN HORIZONTAL FINAL ===
    	    double acceleration = netForce / PLANE_MASS;

    	    return acceleration; // en m/s²
    	}

    
    /*
    public double calcualteSpeedIncreaseTAS(double thrust, double airDensity, double airBrakeLevel, double pitchDegrees) {
    double currentSpeedHorizontalSpeed = this.getSpeedTAS();
    double relativeSpeed = currentSpeedHorizontalSpeed;

    double pitchRad = Math.toRadians(pitchDegrees);
    double FThrustTotal = (thrust / 100.0) * MAX_THRUST_FORCE;
    double FThrustHorizontal = FThrustTotal * Math.cos(pitchRad);

    double effectiveCd = DRAG_COEFFICIENT + (AIR_BRAKE_DRAG_COEFFICIENT * airBrakeLevel);
    double dragForce = 0.5 * effectiveCd * airDensity * SpeedSensor.FRONTAL_AREA * Math.pow(relativeSpeed, 2);

    // Fuerza de rodadura
    double frictionCoefficient = 0.03; // coeficiente típico para rodaje en pista
    double gravity = 9.81; // m/s²
    double rollingResistanceForce = frictionCoefficient * PLANE_MASS * gravity;

    // La fuerza de rodadura siempre se opone al movimiento, si la velocidad es 0 no tiene efecto
    if (relativeSpeed > 0) {
        rollingResistanceForce = rollingResistanceForce;
    } else {
        rollingResistanceForce = 0;
    }

    // Sumamos todas las fuerzas que frenan (drag + rodadura) y restamos empuje
    double netForce = FThrustHorizontal - dragForce - rollingResistanceForce;

    double acceleration = netForce / PLANE_MASS; // m/s²
    System.out.println("Net Force: " + netForce + ", Acceleration: " + acceleration);
    return acceleration; // devuelve en m/s²
}
*/
    
    public double calculateGroundSpeed(double tas, double windSpeed, double windAngleDegrees) {
        double windAngleRad = Math.toRadians(windAngleDegrees);
        double windSpeedKmH = windSpeed;
        double windAlongTrack = windSpeedKmH * Math.cos(windAngleRad);
        double gs = tas - windAlongTrack; // + cola, - frente
        return gs;
    }
	 
	 @Override
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
	}
	
	  public double clampSpeed(double speed) {
	       
			return Math.max(MIN_SPEED, Math.min(speed, MAX_SPEED));
	    }

	  public class FADECListener implements ServiceListener {
		    private final BundleContext context;
		    private final SpeedSensor speedSensor;

		    // Guardar último valor para evitar ejecuciones redundantes
		    private Double lastAcceleration = null;

		    public FADECListener(BundleContext context, SpeedSensor speedSensor) {
		        this.context = context;
		        this.speedSensor = speedSensor;
		    }

		    public void start() {
		        String filter = "(|(" + Constants.OBJECTCLASS + "=" + IFADEC.class.getName() + ")"
		                + "(" + Constants.OBJECTCLASS + "=" + IWeatherSensor.class.getName() + ")"
		                + "(" + Constants.OBJECTCLASS + "=" + IAttitudeSensor.class.getName() + ")"
		                + "(" + Constants.OBJECTCLASS + "=" + IControlSurfaces.class.getName() + "))";
		        try {
		            this.context.addServiceListener(this, filter);
		        } catch (InvalidSyntaxException e) {
		            e.printStackTrace();
		        }
		    }

		    public void stop() {
		        this.context.removeServiceListener(this);
		    }

		    @Override
		    public void serviceChanged(ServiceEvent event) {
		        switch (event.getType()) {
		            case ServiceEvent.REGISTERED:
		            case ServiceEvent.MODIFIED:
		                IFADEC fadec = getService(IFADEC.class);
		                IWeatherSensor weatherSensor = getService(IWeatherSensor.class);
		                IAttitudeSensor attitudeSensor = getService(IAttitudeSensor.class);
		                IControlSurfaces controlSurfaces = getService(IControlSurfaces.class);
		                IRadioAltimeterSensor radioAltimeterSensor = getService(IRadioAltimeterSensor.class);
		                IAltitudeSensor altimeterSensor = getService(IAltitudeSensor.class);

		                if (fadec != null && weatherSensor != null && attitudeSensor != null && controlSurfaces != null) {
		                    double thrust = fadec.getCurrentThrust();
		                    double airDensity = weatherSensor.getAirDensity();
		                    double airBrakeLevel = controlSurfaces.getAirbrakeDeployment();
		                    double pitchDegrees = attitudeSensor.getPitch();
		                    boolean isOnGround = radioAltimeterSensor.isOnGround();
		                    double altitude = altimeterSensor.getAltitude();

		                    double acceleration = speedSensor.calcualteSpeedIncreaseTAS(
		                        thrust, airDensity, airBrakeLevel, pitchDegrees, isOnGround, altitude
		                    );

		                    // Evitar valores fuera de rango
		                    if ((speedSensor.getSpeedTAS() == 0.0 && acceleration < 0.0) ||
		                        (speedSensor.getSpeedTAS() == speedSensor.MAX_SPEED && acceleration > 0.0)) {
		                        acceleration = 0.0;
		                    }

		                    // Solo actualizar si hay cambios reales
		                    if (lastAcceleration == null || Math.abs(acceleration - lastAcceleration) > 0.0001) {
		                        speedSensor.setSpeedIncreaseTAS(acceleration);
		                        lastAcceleration = acceleration;
		                    }
		                }
		                break;

		            case ServiceEvent.UNREGISTERING:
		            case ServiceEvent.MODIFIED_ENDMATCH:
		            default:
		                break;
		        }
		    }

		    // Helper para obtener el primer servicio disponible de un tipo
		    private <T> T getService(Class<T> clazz) {
		        try {
		            ServiceReference<?>[] refs = context.getServiceReferences(clazz.getName(), null);
		            if (refs != null && refs.length > 0) {
		                return clazz.cast(context.getService(refs[0]));
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        return null;
		    }
		}




	
}
