package microsim.simulation.event;

import microsim.simulation.component.SimulationComponent;

/**
 * SimulationEvent that signals simulation owner to attach a {@link ui.DebugShell} instance.
 */
public class AttachEvent extends SimulationEvent {

  /**
   * Instantiates attach event.
   *
   * @param owner component that raised event
   */
  public AttachEvent(SimulationComponent owner) {
    super(owner);
  }

  /**
   * Returns information about the event.
   *
   * @return debug string
   */
  @Override
  public String getDebugMessage() {
    return "Simulation signaled to attach debugger";
  }
}
