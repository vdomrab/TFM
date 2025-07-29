package autonomousplane.devices.interfaces;

public interface IFuelSensor {
	 double getFuelLevel();         // Retorna combustible restante (litros o kg)
	    double getFuelPercentage();    // Retorna combustible restante en %
	    IFuelSensor consumeFuel(double amount); // Método para consumir combustible
	    double updateFuelConsumption(double thrustPercentage, double airDensity);
	    IFuelSensor setFuelLevel(double level); 
	    IFuelSensor setFuelPercentage(double percentage);
	    IFuelSensor setFuelConsumptionRate(double rateKgPerSec); // Establece la tasa de consumo de combustible (kg/s)
	    double getFuelConsumptionRate(); // Retorna la tasa de consumo de combustible (kg/s)
	    double getFuelConsumptionRatePercentage(); // Retorna la tasa de consumo de combustible como porcentaje del máximo
	    double getEstimatedEnduranceSeconds();
	    double getEstimatedRangeMeters(double tas);// Retorna la autonomía de combustible en horas
}