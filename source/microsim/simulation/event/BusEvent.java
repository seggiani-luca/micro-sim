package microsim.simulation.event;

import microsim.simulation.component.SimulationComponent;

/**
 * SimulationEvent that represents a bus operation completed by any
 * {@link microsim.simulation.component.SimulationComponent} component.
 */
public class BusEvent extends DebugEvent {

  /**
   * Type of bus operation.
   */
  String type;
  /**
   * Address of operation on bus. Cannot be null.
   */
  char addr;
  /**
   * Data of operation on bus. Can be null.
   */
  Character data;

  /**
   * Instantiates BusEvent getting decode operation information.
   *
   * @param owner Processor that raised DecodeEvent
   * @param type type of operation
   * @param addr address of operation
   * @param data data of operation
   */
  public BusEvent(SimulationComponent owner, String type, char addr, Character data) {
    super(owner);
    this.type = type;
    this.addr = addr;
    this.data = data;
  }

  /**
   * Returns information about what bus operation happened.
   *
   * @return memory operation debug string
   */
  @Override
  public String getDebugMessage() {
    return (owner.getClass().getName() + " initiated " + type + " operation on bus at address "
      + String.format("%04X", addr & 0xffff)
      + (data != null ? " with data " + String.format("%04X", data & 0xffff) : ""));
  }
}
