package autonomousplane.devices.interfaces;



public interface IFADEC {

	public IFADEC setTHRUSTPercentage(double thrust);
	
	public double getCurrentThrust();
	
	public IFADEC setFailure(boolean failure);
	
	public boolean isFailure();
}
