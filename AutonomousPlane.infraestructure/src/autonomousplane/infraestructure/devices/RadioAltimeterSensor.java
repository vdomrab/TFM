package autonomousplane.infraestructure.devices;

import autonomousplane.infraestructure.Thing;
import autonomousplane.infraestructure.autopilot.AbstractAltitudeRateListener;


import org.osgi.framework.BundleContext;


import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.IThing;
public class RadioAltimeterSensor extends Thing implements IRadioAltimeterSensor {

	public static final String GROUND_DISTANCE = "ground_distance"; // Ground distance in meters
	public static final String REALGROUND_ALTITUDE = "REAL_GROUND_ALTITUDE";

	

	public static final double MAX_VERTICAL_ACCELERATION = 25.0;  // ≈ +2.5 g
	public static final double MIN_VERTICAL_ACCELERATION = -15.0; // ≈ -1.5 g
	
	public static final double MAX_GROUND_DISTANCE = 2500.0; 
	public static final double MIN_GROUND_DISTANCE = 0; 
	public static final double MIN_REALGROUND_ALTITUDE = 0.0; // Minimum ground altitude in meters
	public static final double MAX_REALGROUND_ALTITUDE = 6000.0; // Maximum ground altitude in meters
	
	public static final double MAX_VERTICAL_SPEED = 22.0; // Maximum vertical speed in m/s
	public static final double MIN_VERTICAL_SPEED = -25.0; // Minimum vertical speed in m/s
	//public AltitudeGroundRateListener listener = null;
	public RadioAltimeterSensor(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IRadioAltimeterSensor.class.getName());
		this.setGroundDistance(0.0); 
		this.setRealGroundAltitude(0.0); // Initialize real ground altitude to 0 m
		
		//this.listener = new AltitudeGroundRateListener(context, this);
	}

	@Override
	public double getGroundDistance() {
		return (double) this.getProperty(RadioAltimeterSensor.GROUND_DISTANCE);
	}
	
	@Override
	public IRadioAltimeterSensor setGroundDistance(double groundAltitude) {
		double clampedAltitude = Math.max(MIN_GROUND_DISTANCE, Math.min(groundAltitude, RadioAltimeterSensor.MAX_GROUND_DISTANCE));
		this.setProperty(RadioAltimeterSensor.GROUND_DISTANCE, clampedAltitude);
		return this;
	}
	
	public boolean isOnGround() {
	    return this.getGroundDistance() <= 0.001 ;
	}
	
	@Override
	public IRadioAltimeterSensor setRealGroundAltitude(double groundAltitude) {
		double clampedAltitude = Math.max(RadioAltimeterSensor.MIN_REALGROUND_ALTITUDE, Math.min(groundAltitude, RadioAltimeterSensor.MAX_REALGROUND_ALTITUDE));
		this.setProperty(RadioAltimeterSensor.REALGROUND_ALTITUDE, clampedAltitude);
		return this;
	}
	@Override
	public double getRealGroundAltitude() {
		Double value = (Double) this.getProperty(RadioAltimeterSensor.REALGROUND_ALTITUDE);
		return (value != null) ? value.doubleValue() : 0.0; // Default ground altitude is 0
	}
	
	public static double calculateGroundDistance(double altitude, double realgroundAltitude) {
	    return altitude - realgroundAltitude;
	}

	/*@Override
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
	}*/
	
	/*public class AltitudeGroundRateListener extends AbstractAltitudeRateListener {
        private RadioAltimeterSensor altitudeSensor;

        public AltitudeGroundRateListener(BundleContext context, RadioAltimeterSensor altitudeSensor) {
            super(context);
            this.altitudeSensor = altitudeSensor;
        }

        /*@Override
      protected double calculateAltitudeRATE(double speed, double pitchAngle) {
            return altitudeSensor.calculateAltitudeRATE(speed, pitchAngle);
        }

        @Override
        protected void setAltitudeRates(double altitudeRate) {
            altitudeSensor.setAltitudeRates(altitudeRate);
        }
    }*/

	
}
