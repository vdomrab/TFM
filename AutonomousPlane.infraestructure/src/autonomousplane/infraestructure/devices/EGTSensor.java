package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.infraestructure.Thing;

public class EGTSensor extends Thing implements IEGTSensor{
	public static final String TEMPERATURE = "temperature";
	public static final String COOLING_ENABLED = "coolingEnabled"; // Indica si el sistema de refrigeración está activo
	public static final String HEATING_ENABLED = "heatingEnabled"; // Indica si el sistema de calefacción está activo
	public static final double TEMP_MIN_C = 200.0;           // Idle o mínimo posible en marcha
	public static final double TEMP_MAX_C = 1100.0;          // Daño crítico
	public static final double OVERHEAT_THRESHOLD_C = 950.0; // Umbral de advertencia de sobrecalentamiento
	
	// Para detectar formación de hielo en admisión o fuselaje deberías usar temperatura exterior (OAT)
	public static final double ICE_DANGER_OAT_THRESHOLD_C = 5.0; // Riesgo de formación de hielo por OAT
	
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

        double currentTemp = baseEGT + tempAdjustment + pressureAdjustment;

        // Aplicar refrigeración
        if (coolingEnabled) {
            currentTemp -= 100.0;
        }

        // Aplicar calefacción
        if (heatingEnabled) {
            currentTemp += 50.0;
        }

        // Limitar temperatura dentro de los márgenes definidos
        currentTemp = Math.max(TEMP_MIN_C, Math.min(TEMP_MAX_C, currentTemp));
        this.setTemperature(currentTemp, thrust, outsideTempC);

        // Logging opcional de alertas
        if (currentTemp > OVERHEAT_THRESHOLD_C) {
            System.out.println("⚠️ WARNING: Engine Overheating! Temp = " + currentTemp + " °C");
        } else if (outsideTempC < ICE_DANGER_OAT_THRESHOLD_C && humidity > 70.0) {
            System.out.println("⚠️ Ice hazard detected on engine inlet!");
        }

        return this;
    }	
	
	public IEGTSensor setTemperature(double temperature, double thrust, double outsideTempC) {
		if(thrust < 0 ) {
		this.setProperty(EGTSensor.TEMPERATURE,  Math.max(TEMP_MIN_C, Math.min(TEMP_MAX_C, temperature)));
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
