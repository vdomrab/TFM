package autonomousplane.devices.interfaces;

public interface IControlSurfaces {
	  void setAileronDeflection(double deflection);   // ° (positivo = alerón derecho arriba)
	    void setElevatorDeflection(double deflection);  // ° (positivo = nariz arriba)
	    void setRudderDeflection(double deflection);    // ° (positivo = nariz a la derecha)
	    void setAirbrakeDeployment(double deployment); // 0.0 (retraído) a 1.0 (totalmente extendido)
	    
	    double getAirbrakeDeployment(); // 0.0 (retraído) a 1.0 (totalmente extendido)
	    double getAileronDeflection();
	    double getElevatorDeflection();
	    double getRudderDeflection();
	}
