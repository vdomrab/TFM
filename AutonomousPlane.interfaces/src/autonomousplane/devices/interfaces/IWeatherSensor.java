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
	public IWeatherSensor setHumidity(double humidity);
	public double getHumidity();
	public double calculateHumidity(double altitudeMeters);
	public IWeatherSensor setHPA(double hPa);
	public double getHPA();
	public double calculatePressureHpa();
	public IWeatherSensor setInCloud(boolean inCloud);
	public boolean isInCloud();
}
