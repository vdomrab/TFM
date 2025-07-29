package autonomousplane.infraestructure.devices;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IThing;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.Thing;
import autonomousplane.interfaces.EClimate;
import autonomousplane.devices.interfaces.IAltitudeSensor;

public class WeatherSensor extends Thing implements IWeatherSensor{
	public static final String TEMPERATURE = "temperature";
	public static final String GROUNDTEMPERATURE = "Groundtemperature";

	public static final String AIR_DENSITY = "airDensity";
	public static final String WIND_SPEED = "windSpeed";
	public static final String ACTUAL_CLIMATE = "actualClimate";
	public static final String WIND_DIRECTION = "windDirection"; // Wind direction in degrees
	public static final String HUMIDITY = "humidity"; // Humidity in percentage
	public static final String HPA = "pressureHpa"; // Atmospheric pressure in hPa
	public static final String CLOUD = "inAcloud"; // Cloud cover in percentage
	public static final double MAX_TEMPERATURE = 50.0; // Max temperature in Celsius
	public static final double MIN_TEMPERATURE = -50.0; // Min temperature in Celsius
	public static final double MAX_AIR_SPEED = 100.0; // Max wind speed in m/s
	public static final double MIN_AIR_SPEED = -100.0; // Min wind speed in m/s
	public static final double AIR_DENSITY_AT_SEA_LEVEL = 1.225; // kg/m^3 at sea level
	public static final double MIN_HUMIDITY = 0.0; // Min humidity in percentage
	public static final double MAX_HUMIDITY = 100.0; // Max humidity in percentage
	private static altitudeListener listener = null;
	public WeatherSensor(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IWeatherSensor.class.getName());
		this.setTemperature(15.0); // Default temperature in Celsius
		this.setGroundTemperature(15.0); // Default temperature in Celsius

		this.setAirDensity(1.225); // Default air density in kg/m^3
		this.setWindSpeed(0.0); // Default wind speed in m/s
		this.setActualClimate(EClimate.CLEAR); // Default climate condition
		this.setWindDirection(0.0); // Default wind direction in degrees
		this.setHumidity(65.0); // Default humidity in percentage
		this.setHPA(calculatePressureHpa());
		listener = new altitudeListener(context, this);
	}

	@Override
	public double getTemperature() {
		return (double) this.getProperty(WeatherSensor.TEMPERATURE);
	}

	@Override
	public IWeatherSensor setTemperature(double temperature) {
		this.setProperty(WeatherSensor.TEMPERATURE, Math.max(WeatherSensor.MIN_TEMPERATURE, Math.min(temperature, WeatherSensor.MAX_TEMPERATURE)));
		return this;
	}
	
	public double getGroundTemperature() {
		return (double) this.getProperty(WeatherSensor.GROUNDTEMPERATURE);
	}
	public IWeatherSensor setGroundTemperature(double groundTemperature) {
		this.setProperty(WeatherSensor.GROUNDTEMPERATURE,  Math.max(WeatherSensor.MIN_TEMPERATURE, Math.min(groundTemperature, WeatherSensor.MAX_TEMPERATURE)));
		return this;
	}

	@Override
	public double getAirDensity() {
		return (double) this.getProperty(WeatherSensor.AIR_DENSITY);
	}

	@Override
	public IWeatherSensor setAirDensity(double airDensity) {
		this.setProperty(WeatherSensor.AIR_DENSITY, airDensity);
		return this;
	}

	@Override
	public double getWindSpeed() {
		return (double) this.getProperty(WeatherSensor.WIND_SPEED);
	}

	@Override
	public IWeatherSensor setWindSpeed(double windSpeed) {
		this.setProperty(WeatherSensor.WIND_SPEED, windSpeed);
		return this;
	}
	
	@Override
	public double getWindDirection() {
		Double value = (Double) this.getProperty(WeatherSensor.WIND_DIRECTION);
		return (value != null) ? value.doubleValue() : 0.0; // Default to 0 if not set
	}
	@Override
	public IWeatherSensor setWindDirection(double windDirection) {
		this.setProperty(WeatherSensor.WIND_DIRECTION, Math.max(0.0, Math.min(windDirection, 360.0))); // Ensure it's between 0 and 360 degrees
		return this;
	}
	@Override
	public EClimate getActualClimate() {
		return (EClimate) this.getProperty(WeatherSensor.ACTUAL_CLIMATE);
	}

	@Override
	public IWeatherSensor setActualClimate(EClimate actualClimate) {
		this.setProperty(WeatherSensor.ACTUAL_CLIMATE, actualClimate);
		return this;
	}
	
	@Override
	public double getHumidity() {
		return (double) this.getProperty(WeatherSensor.HUMIDITY);
	}
	@Override
	public IWeatherSensor setHumidity(double humidity) {
		this.setProperty(WeatherSensor.HUMIDITY, Math.max(WeatherSensor.MIN_HUMIDITY, Math.min(humidity, WeatherSensor.MAX_HUMIDITY)));
		return this;
	}
	@Override
	public IWeatherSensor setInCloud(boolean inCloud) {
		this.setProperty(WeatherSensor.CLOUD, inCloud);
		return this;
	}
	@Override
	public boolean isInCloud() {
		Boolean value = (Boolean) this.getProperty(WeatherSensor.CLOUD);
		return (value != null) ? value.booleanValue() : false; // Default to false if not set
	}
	
	
	public double calculateAirDensity(double altitudeMeters) {
		    double T0 = 288.15; // K
		    double P0 = 101325; // Pa
		    double L = 0.0065;  // K/m
		    double R = 287.05;  // J/(kg·K)
		    double g = 9.80665; // m/s^2

		    double h = altitudeMeters;

		    double T = T0 - L * h;
		    double P = P0 * Math.pow((T / T0), (g / (R * L)));
		    double rho = P / (R * T);

		    return rho;
	}
	
	public double calculateTemp(double altitudeMeters) {
	    double T0 = this.getGroundTemperature();
	    double L = 0.0065; // Gradiente térmico en K/m
	    double T = T0 - L * altitudeMeters; // Temperatura en grados Celsius
	    if(altitudeMeters > 11000) {
	    	T = -56.5; // En la capa superior de la atmósfera, la temperatura se mantiene constante
	    }
	    return T;
	}
	
	public double calculateHumidity(double altitudeMeters) {
	    // Check if in a cloud
	    boolean inCloud = this.isInCloud();
	    
	    if (inCloud) {
	        return 100.0; // Inside a cloud, RH ≈ 100%
	    }

	    // Normal humidity decay with altitude
	    double seaLevelHumidity = 80.0; // Typical sea-level RH (%)
	    double scaleHeight = 2000.0;    // Decay rate with altitude (m)

	    double humidity = seaLevelHumidity * Math.exp(-altitudeMeters / scaleHeight);

	    return Math.max(0.0, Math.min(100.0, humidity));
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
	
	public double calculatePressureHpa() {
	    double airDensity = this.getAirDensity(); // kg/m³
	    double temperatureC = this.getTemperature(); // °C
	    double temperatureK = temperatureC + 273.15; // Convert to Kelvin
	    double R = 287.05; // Specific gas constant for dry air [J/(kg·K)]

	    double pressurePa = airDensity * R * temperatureK; // P = ρRT
	    double pressureHpa = pressurePa / 100.0; // Convert Pa to hPa

	    return pressureHpa;
	}
	
	public IWeatherSensor setHPA(double hpa) {
		this.setProperty(WeatherSensor.HPA, hpa);
		return this;
	}
	public double getHPA() {
		return (double) this.getProperty(WeatherSensor.HPA);
	}
	
	public class altitudeListener implements ServiceListener {
		private final BundleContext context;
		private final WeatherSensor weatherSensor;
	
		public altitudeListener(BundleContext context, WeatherSensor weatherSensor) {
			this.context = context;
			this.weatherSensor = weatherSensor;
		}
		public void start() {
			String filter = "(" + Constants.OBJECTCLASS + "=" + IAltitudeSensor.class.getName() + ")";
			try {
				this.context.addServiceListener(this, filter);
			} catch (InvalidSyntaxException e) {
				e.printStackTrace();
			}
		}

		public void stop() {
			this.context.removeServiceListener(this);
		}

		public void serviceChanged(ServiceEvent event) {
	        ServiceReference<?> ref = event.getServiceReference();
            System.out.print("FADEC regidsdfsdggsdsgstered: " );

            switch (event.getType()) {
			case ServiceEvent.REGISTERED:
			case ServiceEvent.MODIFIED:
	            IAltitudeSensor altimeterSensor = (IAltitudeSensor) context.getService(ref);
	            System.out.print("FADEC registered: " );
	            if (altimeterSensor != null) {
	               double getAltitude = altimeterSensor.getAltitude();
	               weatherSensor.setAirDensity(weatherSensor.calculateAirDensity(getAltitude));
	               weatherSensor.setTemperature(weatherSensor.calculateTemp(getAltitude));
	               weatherSensor.setHumidity(weatherSensor.calculateHumidity(getAltitude));
	               weatherSensor.setHPA(weatherSensor.calculatePressureHpa());
	               
	            }
					break;
	            case ServiceEvent.UNREGISTERING:
				case ServiceEvent.MODIFIED_ENDMATCH:
				default:
					break;
	       
            }
		}
	}


}
