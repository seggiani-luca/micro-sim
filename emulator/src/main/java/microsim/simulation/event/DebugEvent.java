package microsim.simulation.event;

import microsim.simulation.component.SimulationComponent;
import microsim.ui.DebugShell;

/**
 * SimulationEvent that signals a general event (for debugging purposes) on a simulation component.
 * Used to signal things like memory accesses, successful decoded instructions, etc...
 */
public class DebugEvent extends SimulationEvent {

  /**
   * Debug message associated to this event.
   */
  private final String message;

  /**
   * First int associated to this event.
   */
  private final Integer int0;

  /**
   * Second int associated to this event.
   */
  private final Integer int1;

  /**
   * Instantiates debug event getting debug information.
   *
   * @param owner component that raised event
   * @param message debug message
   */
  public DebugEvent(SimulationComponent owner, String message) {
    super(owner);
    this.message = message;
  }

  /**
   * Instantiates debug event getting debug information with one int.
   *
   * @param owner component that raised event
   * @param message debug message
   * @param int0 first int to concatenate to message
   */
  public DebugEvent(SimulationComponent owner, String message, int int0) {
    super(owner);
    this.message = message;
    this.int0 = int0;
    this.int1 = null;
  }


  /**
   * Instantiates debug event getting debug information with two intes.
   *
   * @param owner component that raised event
   * @param message debug message
   * @param int0 first int to concatenate to message
   * @param int1 second int to concatenate to message
   */
  public DebugEvent(SimulationComponent owner, String message, int int0, int int1) {
    super(owner);
    this.message = message;
    this.int0 = int0;
    this.int1 = int1;
  }


  /**
   * Returns information about the event.
   *
   * @return debug string
   */
  @Override
  public String getDebugMessage() {
    String ret = message + " " DebugShell.int32ToString(int0);
  }
}
