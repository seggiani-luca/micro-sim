package microsim.simulation.event;

import microsim.simulation.component.SimulationComponent;

/**
 * Represents an event raised by a {@link microsim.simulation.component.SimulationComponent} object.
 */
public abstract class SimulationEvent {

  /**
   * Simulation component that raised this event.
   */
  public SimulationComponent owner;

  /**
   * Instantiates SimulationEvent getting a reference to the simulation component that raised it.
   *
   * @param owner simulation component that raised this event
   */
  public SimulationEvent(SimulationComponent owner) {
    this.owner = owner;
  }

  /**
   * Returns a debug string regarding this event.
   *
   * @return debug string
   */
  public String getDebugMessage() {
    return null; // return nothing by default
  }
}
