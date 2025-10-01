package microsim.simulation.event;

import microsim.simulation.component.*;

/**
 * SimulationEvent that signals a memory operation on a
 * {@link microsim.simulation.component.MemorySpace} component.
 */
public class MemoryEvent extends DebugEvent {

  /**
   * Type of operation started.
   */
  String operationType;

  /**
   * Address involved in operation.
   */
  char addr;

  /**
   * Data involved in operation.
   */
  char data;

  /**
   * Instantiates MemoryEvent getting memory operation information.
   *
   * @param owner MemoryDevice that raised MemoryEvent
   * @param operationType type of operation
   * @param addr address of operation
   * @param data data of operation
   */
  public MemoryEvent(MemorySpace owner, String operationType, char addr, char data) {
    super(owner);

    this.operationType = operationType;
    this.addr = addr;
    this.data = data;
  }

  /**
   * Returns information about what type of operation was performed, at which address and to what
   * data.
   *
   * @return memory operation debug string
   */
  @Override
  public String getDebugMessage() {
    return ("Memory saw " + operationType + " operation at address "
      + String.format("%04X", addr & 0xffff) + " of value " + String.format("%04X", data & 0xffff));
  }
}
