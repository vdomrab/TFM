package autonomousplane.simulation.simulator;

import autonomousplane.infraestructure.OSGiUtils;
import autonomousplane.infraestructure.Thing;
import autonomousplane.simulation.interfaces.ISimulationElement;
import java.util.Collection;

import org.osgi.framework.BundleContext;



public class Simulator extends Thing implements ISimulator {

	public static final String STEP = "step";
	protected ISimulatorStepsManager stepsManager = null;
	protected boolean verbose = true;
	protected PlaneSimulationElement simulationPlane = null;
   public Simulator(BundleContext context, String id, IPlaneSimulation simulationElement) {
	   super(context, id);
	   this.setStepCounter(0);
	   this.simulationPlane = (PlaneSimulationElement) simulationElement;
	   this.addImplementedInterface(ISimulator.class.getName());
   }

   @Override
	public ISimulator setStepsManager(ISimulatorStepsManager manager) {
	   this.stepsManager = manager;
	   return this;
	}
   
   @Override
	public ISimulatorStepsManager getStepsManager() {
		return this.stepsManager;
	}
   
   @Override
   public Integer getStepNumber() {
      return (Integer) this.getProperty(Simulator.STEP);
   }

   protected Integer incrementStepCounter() {
      Integer s = this.getStepNumber();
      this.setStepCounter(++s);
      return s;
   }
   
   private void setStepCounter(int step) {
	   this.setProperty(Simulator.STEP, step);
   }

   @Override
   public ISimulator start() {
      if (this.stepsManager != null) {
         this.stepsManager.start();
      }
      return this;
   }

   @Override
   public ISimulator stop() {
      if (this.stepsManager != null) {
         this.stepsManager.stop();
      }
      return this;
   }
   
   @Override
	public ISimulator setVerboseMode(boolean value) {
		this.verbose = value;
		return this;
	}

   @Override
   public ISimulator next(long time_lapse_millis) {

      this.takeSimulationStep(this.incrementStepCounter(), time_lapse_millis);
      return this;
   }

   
   public void takeSimulationStep(Integer step, long time_lapse_millis) {
	   
	   if ( this.verbose )
		   System.out.println("\n>>> >>> >>> >>> >>> >>> >>> >>>\n>>> STEP " + step + "\n>>> >>> >>> >>> >>> >>> >>> >>>");

	   Collection<ISimulationElement> simulationElements = OSGiUtils.getServices(this.getBundleContext(), ISimulationElement.class, null);
	   System.out.println("Simulation elements: " + simulationElements);
	   if ( simulationElements != null && simulationElements.size() > 0 )
		   for (ISimulationElement e : simulationElements) {
			   e.onSimulationStep(step, time_lapse_millis);
		   }
	   this.simulationPlane.onSimulationStep(step, time_lapse_millis);

	   
   }
   
   

   
}
