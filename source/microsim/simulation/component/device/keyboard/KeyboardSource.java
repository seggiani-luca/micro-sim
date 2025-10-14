package microsim.simulation.component.device.keyboard;

/**
 * Implements an input source that can be attached to a
 * {@link microsim.simulation.component.device.keyboard.KeyboardDevice} to send key events (through
 * the {@link microsim.simulation.component.device.keyboard.KeyboardDevice#keyQueue} method.
 */
public abstract class KeyboardSource {

  /**
   * Device instance that listens to this source.
   */
  private KeyboardDevice listener;

  /**
   * Set who the listener is.
   *
   * @param listener device instance that should listen to this source
   */
  public void setListener(KeyboardDevice listener) {
    this.listener = listener;
  }

  /**
   * Accept a new key press event. The k argument can be any character: it is checked to be a valid
   * ASCII character (not extended), and only if it it's passed along. Otherwise it gets ignored.
   *
   * @param k character of key pressed
   */
  void accept(char k) {
    // only accept ASCII
    if ((k & 0x7f) != k) {
      return;
    }

    // insert key in listener queue
    listener.queueKey((byte) k);
  }
}
