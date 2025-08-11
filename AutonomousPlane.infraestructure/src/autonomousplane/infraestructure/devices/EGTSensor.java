package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.infraestructure.Thing;

public class EGTSensor extends Thing implements IEGTSensor{
	public static final String TEMPERATURE = "temperature";
	public static final String COOLING_ENABLED = "coolingEnabled"; // Indica si el sistema de refrigeración está activo
	public static final String HEATING_ENABLED = "heatingEnabled"; // Indica si el sistema de calefacción está activo
	public static final double TEMP_MAX_C = 1100.0;          // Daño crítico
	public static final double OVERHEAT_THRESHOLD_C = 950.0; // Umbral de advertencia de sobrecalentamiento
	
	// Para detectar formación de hielo en admisión o fuselaje deberías usar temperatura exterior (OAT)
	public static final double ICE_DANGER_OAT_THRESHOLD_C = 0; // Riesgo de formación de hielo por OAT
	
	public EGTSensor(BundleContext context, String id) {
		
		super(context, id);
		this.addImplementedInterface(IEGTSensor.class.getName());
		this.setTemperature(15.0, 0, 15); // Temperatura inicial por defecto en °C
		this.setCoolingEnabled(false); // Refrigeración desactivada por defecto
		this.setHeatingEnabled(false); // Calefacción desactivada por defecto
	}
	
	public IEGTSensor updateTemperature(double thrust, double outsideTempC, double pressureHpa, double humidity) {
	    boolean coolingEnabled = this.isCoolingEnabled();
	    boolean heatingEnabled = this.isHeatingEnabled();
	    if (coolingEnabled && heatingEnabled) {
	        throw new IllegalArgumentException("Cooling and heating systems cannot be active at the same time.");
	    }

	    // Caso especial: thrust = 0 y sin heating/cooling → igualar a temperatura exterior
	    if (thrust == 0.0 && !coolingEnabled && !heatingEnabled) {
	        this.setTemperature(outsideTempC, thrust, outsideTempC);
	        return this;
	    }

	    // ISA estándar
	    final double ISA_TEMP_C = 15.0;
	    final double ISA_PRESSURE_HPA = 1013.25;

	    double tempDeviation = outsideTempC - ISA_TEMP_C;
	    double pressureDeviation = ISA_PRESSURE_HPA - pressureHpa;

	    // Temperatura base según thrust (simplificado)
	    double baseEGT = 300.0 + (thrust / 100.0) * 700.0;

	    // Ajustes ISA
	    double tempAdjustment = tempDeviation * 0.5;
	    double pressureAdjustment = pressureDeviation * 0.1;

	    // Target nominal antes de reglas especiales
	    double targetTemp = baseEGT + tempAdjustment + pressureAdjustment;

	    // Reglas para frío extremo y thrust bajo
	    if (outsideTempC < 0.0 && thrust <= 5.0) {
	        double thrustFactor = Math.min(1.0, thrust / 5.0);
	        double extremeTarget = outsideTempC + 2.0;
	        targetTemp = extremeTarget * (1.0 - thrustFactor) + targetTemp * thrustFactor;
	    }

	    // Ajuste manual por sistemas del avión
	    if (coolingEnabled) {
	        targetTemp -= 100.0;
	    }
	    if (heatingEnabled) {
	        targetTemp += 50.0;
	    }

	    // Inercia térmica adaptativa
	    double currentTemp = this.getTemperature();
	    double tempDiff = Math.abs(currentTemp - targetTemp);

	    if (targetTemp < currentTemp) {
	        double coolingRate = Math.min(250.0, 10.0 + 0.25 * tempDiff);
	        if (outsideTempC < 0.0 && thrust <= 5.0) {
	            double extra = (1.0 - Math.min(1.0, thrust / 5.0)) * Math.min(150.0, tempDiff * 0.3);
	            coolingRate += extra;
	        }
	        currentTemp = Math.max(currentTemp - coolingRate, targetTemp);
	    } else {
	        double heatingRate = Math.min(200.0, 20.0 + 0.5 * tempDiff);
	        currentTemp = Math.min(currentTemp + heatingRate, targetTemp);
	    }

	    currentTemp = Math.min(TEMP_MAX_C, currentTemp);

	    this.setTemperature(currentTemp, thrust, outsideTempC);
	    return this;
	}

	
	public IEGTSensor setTemperature(double temperature, double thrust, double outsideTempC) {
		if(thrust < 0 ) {
		this.setProperty(EGTSensor.TEMPERATURE, Math.min(TEMP_MAX_C, temperature));
		}else {
			this.setProperty(EGTSensor.TEMPERATURE,  Math.max(outsideTempC, Math.min(TEMP_MAX_C, temperature)));
		}
		return this;
	}
	
	public double getTemperature() {
		return (double) this.getProperty(EGTSensor.TEMPERATURE);
	}
	
	public IEGTSensor setCoolingEnabled(boolean coolingEnabled) {
		this.setProperty(EGTSensor.COOLING_ENABLED, coolingEnabled);
		if (coolingEnabled && this.isHeatingEnabled()) {
			throw new IllegalArgumentException("Cooling and heating systems cannot be active at the same time.");
		}
		return this;
	}
	public boolean isCoolingEnabled() {
		return (boolean) this.getProperty(EGTSensor.COOLING_ENABLED);
	}
	public IEGTSensor setHeatingEnabled(boolean heatingEnabled) {
		if (this.isCoolingEnabled() && heatingEnabled) {
			throw new IllegalArgumentException("Cooling and heating systems cannot be active at the same time.");
		}
		this.setProperty(EGTSensor.HEATING_ENABLED, heatingEnabled);
		return this;
	}
	public boolean isHeatingEnabled() {
		return (boolean) this.getProperty(EGTSensor.HEATING_ENABLED);
	}

}
