package microsim.simulation.event;

import microsim.simulation.Simulation;

/**
 * SimulationEvent that represents a simulation cycle.
 */
public class CycleEvent extends SimulationEvent {

  /**
   * Cycle reached.
   */
  public long cycle;

  /**
   * Instantiates cycle event getting cycle information.
   *
   * @param owner simulation that raised cycle event
   * @param cycle cycle reached
   */
  public CycleEvent(Simulation owner, long cycle) {
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
    return "Cycle " + cycle;
  }
}
