package microsim.simulation.event;

import microsim.simulation.component.*;

/**
 * SimulationEvent that signals a debug event on a simulation component.
 */
public class DebugEvent extends SimulationEvent {

  String message;

  /**
   * Instantiates DebugEvent getting memory operation information.
   *
   * @param owner component that raised event
   * @param operationType
   */
  public DebugEvent(SimulationComponent owner, String operationType) {
    super(owner);
  }

  /**
   * Returns information about the event.
   *
   * @return memory operation debug string
   */
  @Override
  public String getDebugMessage() {
    return (message);
  }
}
