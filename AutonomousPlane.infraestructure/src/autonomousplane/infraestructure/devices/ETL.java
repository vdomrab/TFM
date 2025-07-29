package autonomousplane.infraestructure.devices;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;

import autonomousplane.devices.interfaces.IETL;
import autonomousplane.infraestructure.Thing;
import autonomousplane.interfaces.EFlyingStages;

public class ETL extends Thing implements IETL{
	public static final String SIGNAL_SENT = "signalSent";
	public static final String SIGNAL_INFO = "emergencyCode";
	public ETL(BundleContext context, String id) {
		super(context, id);
		this.addImplementedInterface(IETL.class.getName());
	
	}
	@Override
	public IETL sendSignalString(String emergencyCode,
			long timestamp,
			double altitude,
			double verticalSpeed,
			double pitch,
			double roll,
			double yaw,
			double airspeed,
			double groundSpeed,
			double angleOfAttack,
			double thrust,
			String destination,
			EFlyingStages phase) {
		Map<String, Object> signal = buildEmergencySignal(emergencyCode, timestamp, altitude, verticalSpeed, pitch, roll, yaw, airspeed, groundSpeed, angleOfAttack, thrust, destination, phase);
		this.setProperty(ETL.SIGNAL_SENT, true);
		this.setProperty(ETL.SIGNAL_INFO, signal);
		return this;
	}
	
	protected  Map<String, Object> buildEmergencySignal(String emergencyCode,
			long timestamp,
			double altitude,
			double verticalSpeed,
			double pitch,
			double roll,
			double yaw,
			double airspeed,
			double groundSpeed,
			double angleOfAttack,
			double thrust,
			String destination,
			EFlyingStages phase) {
	
		Map<String, Object> signal = new HashMap<>();

        signal.put("emergencyCode", emergencyCode);
        signal.put("timestamp", timestamp);
        signal.put("altitude", altitude);
        signal.put("verticalSpeed", verticalSpeed);
        signal.put("pitch", pitch);
        signal.put("roll", roll);
        signal.put("yaw", yaw);
        signal.put("airspeed", airspeed);
        signal.put("groundSpeed", groundSpeed);
        signal.put("angleOfAttack", angleOfAttack);
        signal.put("thrust", thrust);
        signal.put("destination", destination);
        signal.put("flightPhase", phase);

        return signal;
	}
	@Override
	public boolean isSignalSent() {
		boolean signalSent = (boolean) this.getProperty(ETL.SIGNAL_SENT);
		return signalSent;
	}
	@Override
	public void resetSignalSent() {
		this.setProperty(ETL.SIGNAL_SENT, false);
		this.setProperty(ETL.SIGNAL_INFO, null);
		
	}
}

