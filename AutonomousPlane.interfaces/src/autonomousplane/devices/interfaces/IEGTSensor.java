package autonomousplane.devices.interfaces;

public interface IEGTSensor {
	IEGTSensor updateTemperature(double thrust, double outsideTempC, double pressureHpa, double humidty);
	IEGTSensor setTemperature(double temperature, double thrust, double outsideTempC);
	double getTemperature();
	
	IEGTSensor setCoolingEnabled(boolean coolingEnabled);
	boolean isCoolingEnabled();
	IEGTSensor setHeatingEnabled(boolean heatingEnabled);
	boolean isHeatingEnabled();
}
