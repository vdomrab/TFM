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
import autonomousplane.interfaces.EFlyingStages;
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
	public void onSimulationStep(Integer step, long timeLapseMillis) {
	    final double dt = PlaneSimulationElement.getTimeStep(); // segundos
	    this.refreshServices();

	    // === 1. Actualización de actitud (pitch, roll, yaw) ===
	    if (attitudeSensor != null) {
	        attitudeSensor.setRoll(attitudeSensor.getRoll() + attitudeSensor.getRollRate() * dt);
	        attitudeSensor.setYaw(attitudeSensor.getYaw() + attitudeSensor.getYawRate() * dt);
	        attitudeSensor.setPitch(attitudeSensor.getPitch() + attitudeSensor.getPitchRate() * dt);
	    }

	    // === 2. Velocidad TAS y GS ===
	    if (speedSensor != null && weatherSensor != null && fadec != null && attitudeSensor != null && controlSurfaces != null) {
	        double newTAS = speedSensor.getSpeedTAS() + (speedSensor.getSpeedIncreaseTAS() * dt);
	        speedSensor.setSpeedTAS(newTAS);

	        speedSensor.setSpeedGS(
	            speedSensor.calculateGroundSpeed(
	                newTAS,
	                weatherSensor.getWindSpeed(),
	                weatherSensor.getWindDirection()
	            )
	        );
	    }

	    // === 3. Altitud, aceleración vertical y AOA ===
	    if (speedSensor != null && weatherSensor != null && fadec != null &&
	        attitudeSensor != null && controlSurfaces != null &&
	        altimeterSensor != null && radioAltimeterSensor != null &&
	        navigationSystem != null && aoaSensor != null) {

	        double aoa = aoaSensor.calculateAOA(
	            speedSensor.getSpeedTAS(),
	            altimeterSensor.getVerticalSpeed(),
	            attitudeSensor.getPitch()
	        );

	        double verticalAcc = altimeterSensor.calculateVerticalAcceleration(
	            fadec.getCurrentThrust(),
	            attitudeSensor.getPitch(),
	            weatherSensor.getAirDensity(),
	            speedSensor.getSpeedTAS(),
	            aoa
	        );

	        // Ajustes en tierra o en altitud máxima
	        if ((radioAltimeterSensor.isOnGround() && verticalAcc <= 0) ||
	            (altimeterSensor.getAltitude() == AltitudeSensor.MAX_ALTITUDE && verticalAcc > 0)) {
	            altimeterSensor.setVerticalSpeed(0);
	            altimeterSensor.setVerticalAcceleration(0);
	            altimeterSensor.setAltitude(radioAltimeterSensor.getRealGroundAltitude());
	        } else {
	            altimeterSensor.setVerticalAcceleration(verticalAcc);

	            // Nueva velocidad vertical
	            double newVSpeed = altimeterSensor.getVerticalSpeed() + (verticalAcc * dt);
	            altimeterSensor.setVerticalSpeed(newVSpeed);

	            // Nueva altitud
	            double newAltitude = altimeterSensor.getAltitude() + (newVSpeed * dt);
	            if (newAltitude < radioAltimeterSensor.getRealGroundAltitude()) {
	                newAltitude = radioAltimeterSensor.getRealGroundAltitude();
	            }
	            altimeterSensor.setAltitude(newAltitude);

	            // Distancia al suelo
	            radioAltimeterSensor.setGroundDistance(
	                RadioAltimeterSensor.calculateGroundDistance(
	                    altimeterSensor.getAltitude(),
	                    radioAltimeterSensor.getRealGroundAltitude()
	                )
	            );
	        }

	        // Mantener pitch en 0 si está en tierra y en actitud descendente
	        if (radioAltimeterSensor.isOnGround() && attitudeSensor.getPitch() < 0) {
	            attitudeSensor.setPitch(0);
	        }

	        // Sensor de proximidad
	        if (radioAltimeterSensor.getGroundDistance() > 150.0 && proximitySensor != null) {
	            proximitySensor.setObjectDetected(false);
	        }
	    }

	    // === 4. Navegación y etapa de vuelo ===
	    if (speedSensor != null && navigationSystem != null && attitudeSensor != null && radioAltimeterSensor != null) {
	        if (speedSensor.getSpeedGS() != 0) {
	            navigationSystem.calcualteCurrentDistance(speedSensor.getSpeedGS() * dt);
	        }
	        if (altimeterSensor != null) {
	            double altitudeAGL = altimeterSensor.getAltitude() - radioAltimeterSensor.getRealGroundAltitude();
	            navigationSystem.setCurrentFlyghtStage(
	                navigationSystem.calculateTheFlyingStage(
	                    altitudeAGL,
	                    navigationSystem.getCurrentDistance(),
	                    navigationSystem.getTotalDistance(),
	                    attitudeSensor.getPitch()
	                )
	            );
	        }
	    }

	    // === 5. AOA persistente ===
	    if (aoaSensor != null && speedSensor != null && altimeterSensor != null && attitudeSensor != null) {
	        aoaSensor.setAOA(
	            aoaSensor.calculateAOA(
	                speedSensor.getSpeedTAS(),
	                altimeterSensor.getVerticalSpeed(),
	                attitudeSensor.getPitch()
	            )
	        );
	    }

	    // === 6. Combustible ===
	    if (fuelSensor != null) {
	        double fuelConsumed = fuelSensor.getFuelConsumptionRate() * dt;
	        fuelSensor.consumeFuel(fuelConsumed);
	    }

	    // === 7. EGT ===
	    if (egtSensor != null && fadec != null && weatherSensor != null &&
	        attitudeSensor != null && controlSurfaces != null && speedSensor != null) {
	        egtSensor.updateTemperature(
	            fadec.getCurrentThrust(),
	            weatherSensor.getTemperature(),
	            weatherSensor.calculatePressureHpa(),
	            weatherSensor.getHumidity()
	        );
	    }
	}

	
	private void refreshServices() {
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


}
