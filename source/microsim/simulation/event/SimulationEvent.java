package microsim.simulation.event;

import microsim.simulation.component.*;

/**
 * Represents an event raised by a {@link microsim.simulation.component.SimulationComponent} object.
 */
public abstract class SimulationEvent {

  SimulationComponent owner;

  /**
   * Instantiates SimulationEvent getting a reference to the SimulationComponent that raised it.
   *
   * @param owner SimulationComponent that raised SimulationEvent
   */
  public SimulationEvent(SimulationComponent owner) {
    this.owner = owner;
  }
}
