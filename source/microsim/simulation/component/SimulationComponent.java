package microsim.simulation.component;

import java.util.ArrayList;
import java.util.List;
import microsim.simulation.event.*;

/**
 * A component simulated within the {@link microsim.simulation.Simulation} class. Implements
 * functionality for stepping and raising {@link microsim.simulation.event.SimulationEvent} events.
 */
public abstract class SimulationComponent {

  /**
   * Array of event listeners.
   */
  private final List<SimulationListener> listeners = new ArrayList<>();

  /**
   * Adds an event listener to {@link #listeners}.
   *
   * @param l listener to add
   */
  public void addListener(SimulationListener l) {
    listeners.add(l);
  }

  /**
   * Removes an event listener from {@link #listeners}. If the listener is not present, nothing
   * changes.
   *
   * @param l listener to add
   */
  public void removeListener(SimulationListener l) {
    listeners.remove(l);
  }

  /**
   * Raises a simulation event.
   *
   * @param e event to raise
   */
  public void raiseEvent(SimulationEvent e) {
    for (SimulationListener l : listeners) {
      l.onSimulationEvent(e);
    }
  }

  /**
   * Is called on simulation steps. Simulation components perform their update logic here.
   */
  public abstract void step();
}
