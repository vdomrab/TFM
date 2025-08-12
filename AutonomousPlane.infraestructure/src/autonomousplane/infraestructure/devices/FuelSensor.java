package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.IThing;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.Thing;

public class FuelSensor extends Thing implements IFuelSensor {
	public final static String FUEL_LEVEL = "FUEL_LEVEL"; 
	public final static String FUEL_CONSUMPTION_RATE = "FUEL_CONSUMPTION_RATE"; // kg/s
	public final static String LOW_FUEL_MODE = "LOW_FUEL_MODE"; // boolean, true if low fuel mode is active
	public final static double MIN_FUEL_LEVEL = 0.0; 
	public static final double MAX_FUEL_KG = 26000.0;
	public static final double MAX_FLOW_RATE_KG_PER_SEC = 2.5; // consumo máx. a 100% thrust
	public static final double IDLE_BURN_RATE_KG_PER_SEC = 0.1; // At 0% thrust (idle)
    protected FuelSensorListener listener = null; // Listener para eventos de combustible
   
    public FuelSensor(BundleContext context, String id) {
        super(context, id);
        this.addImplementedInterface(IFuelSensor.class.getName());
        this.setFuelLevel(MAX_FUEL_KG);
        this.setFuelConsumptionRate(IDLE_BURN_RATE_KG_PER_SEC);
        this.listener = new FuelSensorListener(context, this);
        this.setLowFuelMode(false); // Inicializar sin modo de bajo combustible
        // Inicializar con tasa máxima
    }
    
    @Override
    public IFuelSensor setLowFuelMode(boolean mode) {
		this.setProperty(LOW_FUEL_MODE, mode);
		return this;
	}
    
    @Override
    public boolean isLowFuelMode() {
		Object mode = this.getProperty(LOW_FUEL_MODE);
		return mode != null && (boolean) mode;
	}
    
    
    @Override
    public double getFuelLevel() {
        return (double) this.getProperty(FUEL_LEVEL);
    }
    
    @Override
    public double getFuelPercentage() {
        return (getFuelLevel() / MAX_FUEL_KG) * 100.0;
    }

    @Override
    public IFuelSensor consumeFuel(double amountKg) {
        double current = getFuelLevel();
        double updated = Math.max(0, current - amountKg);
        this.setProperty(FUEL_LEVEL, updated);
        return this;
    }
    @Override
    public double updateFuelConsumption(double thrustPercentage, double airDensity) {
        // Clamp thrust between 0% and 100%
        thrustPercentage = Math.max(0.0, Math.min(100.0, thrustPercentage));

        // Base fuel flow from thrust percentage
        double flowRateKgPerSec = (thrustPercentage / 100.0) * MAX_FLOW_RATE_KG_PER_SEC;

        // Enforce idle fuel consumption if flow is too low
        flowRateKgPerSec = Math.max(flowRateKgPerSec, IDLE_BURN_RATE_KG_PER_SEC);

        // Adjust for air density (lower density -> more fuel burn)
        double standardDensity = 1.225; // kg/m³
        double densityAdjustmentFactor = 1.0 + (standardDensity - airDensity) * 0.05;
        flowRateKgPerSec *= densityAdjustmentFactor;

        if (isLowFuelMode()) {
        	System.out.println("Low fuel mode active, reducing fuel consumption rate by 30%");
            flowRateKgPerSec *= 0.7; // 30% reduction
        }
        return flowRateKgPerSec;       
    }

    @Override
    public IFuelSensor setFuelConsumptionRate(double rateKgPerSec) {
		if (rateKgPerSec < 0 || rateKgPerSec > MAX_FLOW_RATE_KG_PER_SEC) {
			System.out.println("Fuel consumption rate must be between 0 and " + MAX_FLOW_RATE_KG_PER_SEC + " kg/s");
			return this;
		}
		this.setProperty(FUEL_CONSUMPTION_RATE, rateKgPerSec);
		return this;
	}
    
    @Override
    public double getFuelConsumptionRate() {
		return (double) this.getProperty(FUEL_CONSUMPTION_RATE);
	}
    @Override
    public double getFuelConsumptionRatePercentage() {
    	double rate = getFuelConsumptionRate();
    	if (rate < 0 || rate > MAX_FLOW_RATE_KG_PER_SEC) {
			System.out.println("Fuel consumption rate must be between 0 and " + MAX_FLOW_RATE_KG_PER_SEC + " kg/s");
			return 0.0;
		}
    	return (rate / MAX_FLOW_RATE_KG_PER_SEC) * 100.0;
    }
    
    public double getEstimatedEnduranceSeconds() {
        double currentConsumptionRate = getFuelConsumptionRate(); // kg/s
        if (currentConsumptionRate <= 0.0) {
            return Double.POSITIVE_INFINITY; // Consumo nulo, autonomía infinita teórica
        }
        return getFuelLevel() / currentConsumptionRate; // tiempo en segundos
    }
    
    @Override
    public double getEstimatedRangeMeters(double tas) {
        double enduranceSeconds = getEstimatedEnduranceSeconds();
        double trueAirspeed = tas; // your method to get TAS
        return enduranceSeconds * trueAirspeed;
    }
    
    @Override
    public IFuelSensor setFuelLevel(double level) {
		if (level < MIN_FUEL_LEVEL || level > MAX_FUEL_KG) {
			System.out.println("Fuel level must be between " + MIN_FUEL_LEVEL + " and " + MAX_FUEL_KG + " kg");
			return this;
		}
		this.setProperty(FUEL_LEVEL, level);
		return this;
	}
    
    @Override
	public IFuelSensor setFuelPercentage(double percentage) {
		if (percentage < 0 || percentage > 100) {
			System.out.println("Value must be between 0 and 100");
			return this;
		}
		double level = (percentage / 100.0) * MAX_FUEL_KG;
		return setFuelLevel(level);
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
	class FuelSensorListener implements ServiceListener {
	    private BundleContext context;
	    private FuelSensor sensor;
	    private Double lastConsumptionRate = null; // último valor

	    public FuelSensorListener(BundleContext context, FuelSensor sensor) {
	        this.context = context;
	        this.sensor = sensor;
	    }

	    public void start() {
	    	String filter = "(|(" + Constants.OBJECTCLASS + "=" + IFADEC.class.getName() + ")"
	                + "(" + Constants.OBJECTCLASS + "=" + IWeatherSensor.class.getName() + "))";
	               
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
	        IFADEC fadec = getService(IFADEC.class);
	        IWeatherSensor weatherSensor = getService(IWeatherSensor.class);

	        if (fadec == null || weatherSensor == null) {
	            return;
	        }

	        double thrustPercentage = fadec.getCurrentThrust();
	        double airDensity = weatherSensor.getAirDensity();
	        double consumptionRate = sensor.updateFuelConsumption(thrustPercentage, airDensity);

	        switch (event.getType()) {
	            case ServiceEvent.REGISTERED:
	            case ServiceEvent.MODIFIED:
	                // Solo actualizar si ha cambiado
	                if (lastConsumptionRate == null || lastConsumptionRate != consumptionRate) {
	                    sensor.setFuelConsumptionRate(consumptionRate);
	                    lastConsumptionRate = consumptionRate;
	                }
	                break;
	            case ServiceEvent.UNREGISTERING:
	            case ServiceEvent.MODIFIED_ENDMATCH:
	                sensor.setFuelConsumptionRate(0.0);
	                lastConsumptionRate = 0.0;
	                break;
	        }
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
