package microsim.simulation.event;

/**
 * Interface for listeners of {@link microsim.simulation.event.SimulationEvent} events.
 */
public interface SimulationListener {

  /**
   * Gets called when an event reaches the current object.
   *
   * @param e event received
   */
  void onSimulationEvent(SimulationEvent e);
}
