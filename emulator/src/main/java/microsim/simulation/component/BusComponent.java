package microsim.simulation.component;

import microsim.simulation.Simulation;
import microsim.simulation.component.bus.Bus;

/**
 * Represents components that are expected to be mounted on a bus and take a reference to it.
 */
public abstract class BusComponent extends SimulationComponent {

  /**
   * Reference to the communication bus the component is mounted on.
   */
  public Bus bus;

  /**
   * Instantiates bus component, taking a reference to the bus it's mounted to and the simulation it
   * belongs to.
   *
   * @param bus bus this component is mounted on
   * @param simulation simulation this component belongs to
   */
  public BusComponent(Bus bus, Simulation simulation) {
    super(simulation);
    this.bus = bus;
  }
}
