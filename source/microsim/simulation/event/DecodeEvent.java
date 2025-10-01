package microsim.simulation.event;

import microsim.simulation.component.*;
import microsim.simulation.component.Processor.ProcessorState;

/**
 * SimulationEvent that represents a decode operation completed by a
 * {@link microsim.simulation.component.Processor} component.
 */
public class DecodeEvent extends SimulationEvent {

  /**
   * Opcode to decode.
   */
  char opcode;

  /**
   * Decoded execution state.
   */
  ProcessorState execState;

  /**
   * Decoded source register index.
   */
  int sourceIndex;

  /**
   * Decoded destination register index
   */
  int destIndex;

  /**
   * Signals if immediate operand is needed.
   */
  boolean hasImmediate;

  /**
   * Instantiates DecodeEvent getting decode operation information.
   *
   * @param owner Processor that raised DecodeEvent
   * @param opcode opcode to decode
   * @param execState decoded execution state
   * @param sourceIndex decoded source register index
   * @param destIndex decoded source destination index
   * @param hasImmediate signals if immediate operand is needed
   */
  public DecodeEvent(Processor owner, char opcode, ProcessorState execState, int sourceIndex,
    int destIndex, boolean hasImmediate) {
    super(owner);
    this.opcode = opcode;
    this.execState = execState;
    this.sourceIndex = sourceIndex;
    this.destIndex = destIndex;
    this.hasImmediate = hasImmediate;
  }

  /**
   * Returns information about what type of instruction was decoded and its operands.
   *
   * @return decode operation debug string
   */
  @Override
  public String getDebugMessage() {
    return ("Processor decoded opcode " + String.format("%04X", opcode & 0xffff) + " to state "
      + execState.name() + "\nSource register index: " + sourceIndex
      + "\nDestination register index: " + destIndex
      + (hasImmediate ? " \nImmediate read required" : ""));
  }
}
