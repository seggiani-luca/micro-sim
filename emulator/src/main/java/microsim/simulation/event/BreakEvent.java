package microsim.simulation.event;

import microsim.simulation.component.processor.Processor;

/**
 * SimulationEvent that signals simulation {@link ui.DebugShell} instance to activate.
 */
public class BreakEvent extends SimulationEvent {

  /**
   * Instantiates break event.
   *
   * @param owner processor that raised event
   */
  public BreakEvent(Processor owner) {
    super(owner);
  }

  /**
   * Returns information about the event.
   *
   * @return debug string
   */
  @Override
  public String getDebugMessage() {
    return "Simulation signaled to break";
  }
}
