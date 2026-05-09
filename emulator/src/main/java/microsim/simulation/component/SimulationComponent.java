package microsim.simulation.component;

import java.util.ArrayList;
import java.util.List;
import microsim.simulation.Simulation;
import microsim.simulation.event.*;
import microsim.ui.DebugShell;

/**
 * A component simulated within the {@link microsim.simulation.Simulation} class. Implements
 * functionality for stepping on simulation cycles, and for raising
 * {@link microsim.simulation.event.SimulationEvent} events. Components keep a reference to the
 * simulation they belong to.
 */
public abstract class SimulationComponent {

  /**
   * Simulation this component belongs to.
   */
  public Simulation simulation;

  /**
   * Array of event listeners.
   */
  private final List<SimulationListener> listeners = new ArrayList<>();

  /**
   * Instantiates component, taking a reference to the simulation it belongs to.
   *
   * @param simulation simulation this component belongs to
   */
  public SimulationComponent(Simulation simulation) {
    this.simulation = simulation;
  }

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
   * Raises a debug event, that is an event that is only relevant when debugging is enabled.
   * Otherwise, discard the event.
   *
   * @param e event to raise
   */
  public void raiseDebugEvent(SimulationEvent e) {
    if (DebugShell.isDebuggingEnabled()) {
      raiseEvent(e);
    }
  }

  /**
   * Is called on simulation steps. Simulation components perform their update logic here.
   */
  public abstract void step();
}
