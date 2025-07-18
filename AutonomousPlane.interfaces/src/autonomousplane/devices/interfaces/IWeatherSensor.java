package autonomousplane.devices.interfaces;

import autonomousplane.interfaces.EClimate;

public interface IWeatherSensor {
	public IWeatherSensor setTemperature(double temperature);
	public double getTemperature();
	public IWeatherSensor setGroundTemperature(double groundTemperature);
	public double getGroundTemperature();
	public IWeatherSensor setAirDensity(double airDensity);
	public double getAirDensity();
	public IWeatherSensor setWindSpeed(double windSpeed);
	public double getWindSpeed();
	public IWeatherSensor setWindDirection(double windDirection);
	public double getWindDirection();
	public IWeatherSensor setActualClimate(EClimate actualClimate);
	public EClimate getActualClimate();
	public double calculateTemp(double altitude);
}
