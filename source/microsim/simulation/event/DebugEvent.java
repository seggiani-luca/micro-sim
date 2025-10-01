package microsim.simulation.event;

import microsim.simulation.component.SimulationComponent;

/**
 * Represents special event types useful for debugging.
 */
public abstract class DebugEvent extends SimulationEvent {

  /**
   * Instantiates DebugEvent getting a reference to the SimulationComponent that raised it.
   *
   * @param owner SimulationComponent that raised SimulationEvent
   */
  public DebugEvent(SimulationComponent owner) {
    super(owner);
  }

  /**
   * Returns a debug string regarding this event.
   *
   * @return debug string
   */
  public abstract String getDebugMessage();

}
