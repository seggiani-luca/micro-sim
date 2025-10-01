package microsim.simulation.event;

import microsim.simulation.component.*;

/**
 * SimulationEvent that represents a simulation cycle.
 */
public class CycleEvent extends SimulationEvent {

  /**
   * Cycle reached.
   */
  long cycle;

  /**
   * Instantiates CycleEvent getting cycle information.
   *
   * @param owner SimulationComponent that raised CycleEvent
   * @param cycle cycle reached
   */
  public CycleEvent(SimulationComponent owner, long cycle) {
    super(owner);
    this.cycle = cycle;
  }

  /**
   * Returns information about which cycle was reached.
   *
   * @return cycle debug string
   */
  @Override
  public String getDebugMessage() {
    return ("\nCycle " + cycle);
  }
}
