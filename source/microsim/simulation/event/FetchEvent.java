package microsim.simulation.event;

import microsim.simulation.component.processor.*;

/**
 * SimulationEvent that represents a fetch operation completed by a
 * {@link microsim.simulation.component.Processor} component.
 */
public class FetchEvent extends SimulationEvent {

  /**
   * Address of fetched opcode.
   */
  char ip;

  /**
   * Instantiates FetchEvent getting decode operation information.
   *
   * @param owner Processor that raised DecodeEvent
   * @param ip address of fetched opcode
   */
  public FetchEvent(Processor owner, char ip) {
    super(owner);
    this.ip = ip;
  }

  /**
   * Returns information about where the fetch operation happened.
   *
   * @return fetch operation debug string
   */
  @Override
  public String getDebugMessage() {
    return ("Processor is fetching from IP " + String.format("%04X", ip & 0xffff));
  }
}
