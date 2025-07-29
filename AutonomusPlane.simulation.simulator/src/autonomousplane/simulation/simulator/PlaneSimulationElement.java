package autonomousplane.simulation.simulator;

import org.osgi.framework.BundleContext;

import autonomousplane.autopilot.interfaces.IFlyingService;
import autonomousplane.devices.interfaces.IAOASensor;
import autonomousplane.devices.interfaces.IAltitudeSensor;
import autonomousplane.devices.interfaces.IAttitudeSensor;
import autonomousplane.devices.interfaces.IControlSurfaces;
import autonomousplane.devices.interfaces.IEGTSensor;
import autonomousplane.devices.interfaces.IFADEC;
import autonomousplane.devices.interfaces.IFuelSensor;
import autonomousplane.devices.interfaces.ILandingSystem;
import autonomousplane.devices.interfaces.INavigationSystem;
import autonomousplane.devices.interfaces.IProximitySensor;
import autonomousplane.devices.interfaces.IRadioAltimeterSensor;
import autonomousplane.devices.interfaces.ISpeedSensor;
import autonomousplane.devices.interfaces.IWeatherSensor;
import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.Thing;
import autonomousplane.infraestructure.devices.AltitudeSensor;
import autonomousplane.infraestructure.devices.RadioAltimeterSensor;
import autonomousplane.simulation.interfaces.ISimulationElement;

public class PlaneSimulationElement extends Thing implements IPlaneSimulation {
	protected static double time_step = 1;
	public static final double MIN_TIME_STEP = 0.1; // Minimum time step in seconds
	public static final double MAX_TIME_STEP = 10.0; // Maximum time step in seconds
	protected BundleContext context;
	IAOASensor aoaSensor;
    IFlyingService flyingService;
    IAttitudeSensor attitudeSensor;
    IControlSurfaces controlSurfaces;
    IAltitudeSensor altimeterSensor;
    ISpeedSensor speedSensor;
    IRadioAltimeterSensor radioAltimeterSensor;
    IFADEC fadec;
    INavigationSystem navigationSystem;
    IWeatherSensor weatherSensor;
    IFuelSensor fuelSensor;
    IEGTSensor egtSensor;
    ILandingSystem landingSystem;
    IProximitySensor proximitySensor;
    
	public PlaneSimulationElement(BundleContext context, String id) {
		  	super(context, id);
			this.context = context;
			this.addImplementedInterface(IPlaneSimulation.class.getName());
			setTimeStepSeconds(3);
			aoaSensor = OSGiUtils.getService(context, IAOASensor.class);
	        flyingService = OSGiUtils.getService(context, IFlyingService.class);
	        attitudeSensor = OSGiUtils.getService(context, IAttitudeSensor.class);
	        controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
	        altimeterSensor = OSGiUtils.getService(context, IAltitudeSensor.class);
	        speedSensor = OSGiUtils.getService(context, ISpeedSensor.class);
	        radioAltimeterSensor = OSGiUtils.getService(context, IRadioAltimeterSensor.class);
	        fadec = OSGiUtils.getService(context, IFADEC.class);
	        navigationSystem = OSGiUtils.getService(context, INavigationSystem.class);
	        weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
	        fuelSensor = OSGiUtils.getService(context, IFuelSensor.class);
	        egtSensor = OSGiUtils.getService(context, IEGTSensor.class);
	        landingSystem = OSGiUtils.getService(context, ILandingSystem.class);
	        proximitySensor = OSGiUtils.getService(context, IProximitySensor.class);
	   }

	public static void setTimeStepSeconds(double timeStepSeconds) {
		time_step = timeStepSeconds;
	}
	public static double getTimeStep() {
		return time_step;// Default time step is 1 second
	}
	@Override
	public void onSimulationStep(Integer step, long time_lapse_millis) {
		double timeStepSeconds = this.getTimeStep();
		this.refreshServices();
		// Actualizacion de los angulos
		
		// Actualizacion de la velocidad 
		
		/*if(this.speedSensor != null) {
			if (this.speedSensor.getSpeedIncrease() != 0 && this.speedSensor.getSpeed() != this.speedSensor.getTargetSpeed()) {
				this.speedSensor.setSpeed(this.speedSensor.getSpeed() + this.speedSensor.getSpeedIncrease());
			}else if (this.speedSensor.getSpeed() == this.speedSensor.getTargetSpeed()) {
				  this.speedSensor.setSpeedIncrease(0.0); // mantén velocidad sin acceleracion
			}
		}
		if(this.altimeterSensor != null && this.radioAltimeterSensor != null && this.navigationSystem != null) {

		if (this.altimeterSensor.getAltitudeRate() != 0) {
			this.altimeterSensor.setAltitude(this.altimeterSensor.getAltitude() + this.altimeterSensor.getAltitudeRate());
			this.radioAltimeterSensor.setGroundAltitude(altimeterSensor.getAltitude());
			this.navigationSystem.setCurrentFlyghtStage(this.navigationSystem.calculateTheFlyingStage(this.speedSensor.getSpeed()));
		}else if (this.attitudeSensor.getPitchRate() == 0) {
			  this.altimeterSensor.setAltitudeRates(0); // mantén velocidad sin acceleracion
			  this.radioAltimeterSensor.setAltitudeRates(0);
		}
		}
		if(this.speedSensor != null && this.navigationSystem != null) {
			//Actualizacion de la distancia recorrida
			if(this.speedSensor.getSpeed() != 0) {
				 
				this.navigationSystem.calcualteCurrentDistance(this.speedSensor.getSpeed());
			}
		}*/
		System.out.println("PlaneSimulationElement onSimulationSteasdp: " + this.attitudeSensor  );
	
		if(this.attitudeSensor != null ) {
	        double roll = this.attitudeSensor.getRoll();
	        double yaw = this.attitudeSensor.getYaw();
			double pitch = this.attitudeSensor.getPitch();

			this.attitudeSensor.setRoll(roll + attitudeSensor.getRollRate() * timeStepSeconds);
			this.attitudeSensor.setYaw(yaw + attitudeSensor.getYawRate() * timeStepSeconds);
			this.attitudeSensor.setPitch(pitch + this.attitudeSensor.getPitchRate() * timeStepSeconds);
			System.out.println("PlaneSimulationElement onSimulationStep: " + this.attitudeSensor.getRoll() + " " + this.attitudeSensor.getYaw() + " " + this.attitudeSensor.getPitch());
		}
	
		
		if(speedSensor != null && weatherSensor != null && fadec != null && attitudeSensor != null && controlSurfaces != null) {
			double acceleration = speedSensor.getSpeedIncreaseTAS();
			double deltaSpeedMS = acceleration * timeStepSeconds; // m/s
		    double currentSpeedTAS = speedSensor.getSpeedTAS() + deltaSpeedMS;
		    speedSensor.setSpeedTAS(currentSpeedTAS);
		    speedSensor.setSpeedGS(
		        speedSensor.calculateGroundSpeed(
		            currentSpeedTAS,
		            weatherSensor.getWindSpeed(),
		            weatherSensor.getWindDirection()
		        )
		    );
		    
		}
		
		if(speedSensor != null && weatherSensor != null && fadec != null && attitudeSensor != null && controlSurfaces != null && altimeterSensor != null && radioAltimeterSensor != null && navigationSystem != null && aoaSensor != null) {
			double aoa = this.aoaSensor.calculateAOA(this.speedSensor.getSpeedTAS(), this.altimeterSensor.getVerticalSpeed(), this.attitudeSensor.getPitch());

			double verticalAcceleration = altimeterSensor.calculateVerticalAcceleration(
		            fadec.getCurrentThrust(),
		            attitudeSensor.getPitch(),
		            weatherSensor.getAirDensity(),
		            speedSensor.getSpeedTAS(),
		            aoa
		        );
			if(radioAltimeterSensor.isOnGround() && attitudeSensor.getPitch() < 0) {
				attitudeSensor.setPitch(0); // Mantenemos el ángulo de cabeceo en 0
			}
			if(radioAltimeterSensor.isOnGround() && verticalAcceleration <= 0 || altimeterSensor.getAltitude() == AltitudeSensor.MAX_ALTITUDE && verticalAcceleration > 0) {
			        // Si el avión está en tierra y no hay aceleración vertical, mantenemos la altitud y velocidad
			        altimeterSensor.setVerticalSpeed(0); // Mantén velocidad sin aceleración
					altimeterSensor.setVerticalAcceleration(0); // Mantén velocidad sin aceleración
			        
			} else {
					
					altimeterSensor.setVerticalAcceleration(verticalAcceleration);

				    double deltaV = verticalAcceleration * timeStepSeconds;
				    double newVerticalSpeed = altimeterSensor.getVerticalSpeed() + deltaV;
				    altimeterSensor.setVerticalSpeed(newVerticalSpeed);

				    // ✅ Ahora actualizamos la altitud con el nuevo verticalSpeed y deltaTime
				    double deltaH = altimeterSensor.getVerticalSpeed() * timeStepSeconds;
				    double newAltitude = altimeterSensor.getAltitude() + deltaH;
				    altimeterSensor.setAltitude(newAltitude);
				    
				    radioAltimeterSensor.setGroundDistance(
				    		RadioAltimeterSensor.calculateGroundDistance(
				            altimeterSensor.getAltitude(),
				            radioAltimeterSensor.getRealGroundAltitude()
				        )
				    );

				 
			}
			if(this.radioAltimeterSensor.getGroundDistance()  > 150.0 && this.proximitySensor != null) {
				// Si la distancia al suelo es mayor a 150 metros, ya no hay objeto detectado
				proximitySensor.setObjectDetected(false);
			} 
		        
		}
			
		
		if (speedSensor != null && navigationSystem != null ){
			if(speedSensor.getSpeedGS() != 0 ) {
				navigationSystem.calcualteCurrentDistance(speedSensor.getSpeedGS() * timeStepSeconds);
			}
			if(altimeterSensor != null ) {
		    navigationSystem.setCurrentFlyghtStage(
			        navigationSystem.calculateTheFlyingStage(altimeterSensor.getAltitude())
			    );

			}
		}

		if(this.aoaSensor != null && this.speedSensor != null && this.altimeterSensor != null && this.attitudeSensor != null) {
			double aoa = this.aoaSensor.calculateAOA(this.speedSensor.getSpeedTAS(), this.altimeterSensor.getVerticalSpeed(), this.attitudeSensor.getPitch());
			this.aoaSensor.setAOA(aoa);

		    
		}
		//Actualizacion de la distancia recorrida
		if(this.fuelSensor != null) {
			double fuelConsumed = this.fuelSensor.getFuelConsumptionRate() * timeStepSeconds; // Consumo en kg
			this.fuelSensor.consumeFuel(fuelConsumed); // Actualizar el nivel de combustible
			// Actualizar el consumo de combustible
			
		}
		
		
		if(this.fadec != null && this.weatherSensor != null && this.attitudeSensor != null && this.controlSurfaces != null && this.speedSensor != null) 
		{	
			this.egtSensor.updateTemperature(
		        fadec.getCurrentThrust(),
		        weatherSensor.getTemperature(),
		        weatherSensor.calculatePressureHpa(),
		        weatherSensor.getHumidity());
		}
		
		
		
		
	}
	
	private void refreshServices() {
	    if (aoaSensor == null)
	        aoaSensor = OSGiUtils.getService(context, IAOASensor.class);
	    if (flyingService == null)
	        flyingService = OSGiUtils.getService(context, IFlyingService.class);
	    if (attitudeSensor == null)
	        attitudeSensor = OSGiUtils.getService(context, IAttitudeSensor.class);
	    if (controlSurfaces == null)
	        controlSurfaces = OSGiUtils.getService(context, IControlSurfaces.class);
	    if (altimeterSensor == null)
	        altimeterSensor = OSGiUtils.getService(context, IAltitudeSensor.class);
	    if (speedSensor == null)
	        speedSensor = OSGiUtils.getService(context, ISpeedSensor.class);
	    if (radioAltimeterSensor == null)
	        radioAltimeterSensor = OSGiUtils.getService(context, IRadioAltimeterSensor.class);
	    if (fadec == null)
	        fadec = OSGiUtils.getService(context, IFADEC.class);
	    if (navigationSystem == null)
	        navigationSystem = OSGiUtils.getService(context, INavigationSystem.class);
	    if (weatherSensor == null)
	        weatherSensor = OSGiUtils.getService(context, IWeatherSensor.class);
	    if (fuelSensor == null)
	    	fuelSensor = OSGiUtils.getService(context, IFuelSensor.class);
	    if (egtSensor == null)
	    	egtSensor = OSGiUtils.getService(context, IEGTSensor.class);
	    if (landingSystem == null)
	    	landingSystem = OSGiUtils.getService(context, ILandingSystem.class);
	    if (proximitySensor == null)
	    	proximitySensor = OSGiUtils.getService(context, IProximitySensor.class);
	}


}
