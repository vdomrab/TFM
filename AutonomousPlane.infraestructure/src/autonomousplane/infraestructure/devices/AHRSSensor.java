package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.infraestructure.Thing;
import autonomousplane.devices.interfaces.IThing;
import autonomousplane.devices.interfaces.IWeatherSensor;
public class AHRSSensor extends Thing implements IAttitudeSensor{
	// Attitude angles
	public static final String YAW_ANGLE  = "yaw_angle";
	public static final String PITCH_ANGLE = "pitch_angle";
	public static final String ROLL_ANGLE  = "roll_angle";
	// Angular rates
	public static final String ROLL_RATE = "roll_rate";
	public static final String PITCH_RATE = "pitch_rate";
	public static final String YAW_RATE = "yaw_rate";

	// Angle limits
	public static final double MAX_PITCH = 25.0;
	public static final double MIN_PITCH = -15.0;

	public static final double MAX_ROLL = 45.0;
	public static final double MIN_ROLL = -45.0;

	public static final double MAX_YAW = 360;
	public static final double MIN_YAW = 0;
	// Angular rate limits
	public static final double MAX_ROLL_RATE = 25.0;
	public static final double MIN_ROLL_RATE = -25.0;

	public static final double MAX_PITCH_RATE = 10.0;
	public static final double MIN_PITCH_RATE = -10.0;

	public static final double MAX_YAW_RATE = 10.0;
	public static final double MIN_YAW_RATE = -10.0;
	
	protected ControlSurfaceListener listener = null;

	public AHRSSensor(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IAttitudeSensor.class.getName());
		this.setPitch(0.0);
		this.setRoll(0.0);
		this.setYaw(0.0);
		this.updateAngularRates(0.0, 0.0, 0.0);
		this.listener = new ControlSurfaceListener(context, this);
	}
	
	@Override
	public double getRoll() {
		return (double) this.getProperty(ROLL_ANGLE);
	}
	@Override
	public double getPitch() {
		return (double) this.getProperty(PITCH_ANGLE);
	}
	@Override
	public double getYaw() {
		return (double) this.getProperty(YAW_ANGLE);
	}
	@Override
	public double getRollRate() {
		
		return (double) this.getProperty(ROLL_RATE);
	}

	@Override
	public double getPitchRate() {
		return (double) this.getProperty(PITCH_RATE);
	}

	@Override
	public double getYawRate() {
		return (double) this.getProperty(YAW_RATE);

	}

	@Override
	public IAttitudeSensor setRoll(double roll) {
		double clampedRoll = Math.max(MIN_ROLL, Math.min(roll, MAX_ROLL));
		this.setProperty(ROLL_ANGLE, clampedRoll);
		return this;
	}
	@Override
	public IAttitudeSensor setPitch(double pitch) {
		double clampedPitch = Math.max(MIN_PITCH, Math.min(pitch, MAX_PITCH));
		this.setProperty(PITCH_ANGLE, clampedPitch);
		return this;
	}
	@Override
	public IAttitudeSensor setYaw(double yaw) {
		double clampedYaw = normalizeYaw(yaw);
		this.setProperty(YAW_ANGLE, clampedYaw);
		return this;
	}
	
	@Override
	public IAttitudeSensor updateAngularRates(double rollRate, double pitchRate, double yawRate) {

	    this.setProperty(ROLL_RATE, clampRate(rollRate, MAX_ROLL_RATE, MIN_ROLL_RATE));
	    this.setProperty(PITCH_RATE, clampRate(pitchRate, MAX_PITCH_RATE, MIN_PITCH_RATE));
	    this.setProperty(YAW_RATE, clampRate(yawRate, MAX_YAW_RATE, MIN_YAW_RATE));
	    
	    return this;
	}

	private double clampRate(double rate, double maxRate, double minRate) {
	    return Math.max(minRate, Math.min(rate, maxRate));
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
	public double calculateDensityFactor(IWeatherSensor weatherSensor) {
		if (weatherSensor == null) {
			return 1.0; // Default factor if no weather sensor available
		}
		double airDensity = weatherSensor.getAirDensity();
		double rhoISA = WeatherSensor.AIR_DENSITY_AT_SEA_LEVEL; // densidad del aire a nivel del mar en condiciones ISA
		return Math.sqrt(airDensity / rhoISA); // factor más realista para aerodinámica
		
	}
	
	public double normalizeYaw(double yawDegrees) {
	    yawDegrees = yawDegrees % 360.0;
	    if (yawDegrees < 0) {
	        yawDegrees += 360.0;
	    }
	    return yawDegrees;
	}

	class ControlSurfaceListener implements ServiceListener {
	    private BundleContext context;
	    private AHRSSensor sensor;

	    // Últimos valores para evitar actualizaciones redundantes
	    private Double lastRollRate = null;
	    private Double lastPitchRate = null;
	    private Double lastYawRate = null;

	    public ControlSurfaceListener(BundleContext context, AHRSSensor sensor) {
	        this.context = context;
	        this.sensor = sensor;
	    }

	    public void start() {
	        String filter = "(" + Constants.OBJECTCLASS + "=" + IControlSurfaces.class.getName() + ")";
	        try {
	            this.context.addServiceListener(this, filter);
	        } catch (InvalidSyntaxException e) {

	        }
	    }

	    public void stop() {
	        this.context.removeServiceListener(this);
	    }

	    @Override
	    public void serviceChanged(ServiceEvent event) {
	        ServiceReference<?> ref = event.getServiceReference();
	        IControlSurfaces control = (IControlSurfaces) context.getService(ref);
	        IWeatherSensor weatherSensor = getService(IWeatherSensor.class);

	        if (control == null) {
	            return; // No hay control surfaces
	        }

	        double rollRate;
	        double pitchRate;
	        double yawRate;

	        if (weatherSensor == null) {
	            rollRate = control.getAileronDeflection() * 1.0;
	            pitchRate = control.getElevatorDeflection() * 0.6;
	            yawRate = control.getRudderDeflection() * 0.4;
	        } else {
	            double densityFactor = sensor.calculateDensityFactor(weatherSensor);
	            rollRate = control.getAileronDeflection() * 1.0 * densityFactor;
	            pitchRate = control.getElevatorDeflection() * 0.6 * densityFactor;
	            yawRate = control.getRudderDeflection() * 0.4 * densityFactor;
	        }

	        switch (event.getType()) {
	            case ServiceEvent.REGISTERED:
	            case ServiceEvent.MODIFIED:
	                // Solo actualiza si hay cambios reales
	                if (!valuesAreEqual(rollRate, lastRollRate) ||
	                    !valuesAreEqual(pitchRate, lastPitchRate) ||
	                    !valuesAreEqual(yawRate, lastYawRate)) {

	                    sensor.updateAngularRates(rollRate, pitchRate, yawRate);

	                    lastRollRate = rollRate;
	                    lastPitchRate = pitchRate;
	                    lastYawRate = yawRate;
	                }
	                break;

	            case ServiceEvent.UNREGISTERING:
	            case ServiceEvent.MODIFIED_ENDMATCH:
	                sensor.updateAngularRates(0, 0, 0);
	                lastRollRate = 0.0;
	                lastPitchRate = 0.0;
	                lastYawRate = 0.0;
	                break;
	        }
	    }

	    private boolean valuesAreEqual(Double a, Double b) {
	        if (a == null || b == null) return false;
	        return Math.abs(a - b) < 0.0001; // tolerancia para evitar falsos cambios por decimales
	    }

	    private <T> T getService(Class<T> clazz) {
	        try {
	            ServiceReference<?>[] refs = context.getServiceReferences(clazz.getName(), null);
	            if (refs != null && refs.length > 0) {
	                return clazz.cast(context.getService(refs[0]));
	            }
	        } catch (Exception e) {
	        }
	        return null;
	    }
	}
	}

