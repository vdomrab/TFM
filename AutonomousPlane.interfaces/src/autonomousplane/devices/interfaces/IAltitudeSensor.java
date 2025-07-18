package autonomousplane.devices.interfaces;

public interface IAltitudeSensor {
	
	double getAltitude();
	double getVerticalAcceleration();
	double getVerticalSpeed();
	IAltitudeSensor setAltitude(double altitude);
	IAltitudeSensor setVerticalAcceleration(double altitudeSpeed);
	IAltitudeSensor setVerticalSpeed(double verticalSpeed);
	double calculateAltitudeRATE(double speed, double pitchAngle);
	double calculateVerticalAcceleration(
		    double thrust,            // porcentaje de thrust (0–100)
		    double pitchDegrees,      // ángulo de cabeceo
		    double airDensity,        // ρ en kg/m³ (ISA)
		    double speed,
		    double aoa               // Angle of Attack in degrees
		);
}
