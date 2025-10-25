package microsim.simulation.event;

import microsim.simulation.component.processor.Processor;

/**
 * SimulationEvent that signals simulation owner to power off.
 */
public class HaltEvent extends SimulationEvent {

  /**
   * Instantiates halt event.
   *
   * @param owner processor that raised event
   */
  public HaltEvent(Processor owner) {
    super(owner);
  }

  /**
   * Returns information about the event.
   *
   * @return debug string
   */
  @Override
  public String getDebugMessage() {
    return "Simulation signaled to halt";
  }
}
