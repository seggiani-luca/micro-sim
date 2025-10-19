package microsim.simulation.component.device.keyboard;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 * Implements an input source that can be attached to a
 * {@link microsim.simulation.component.device.keyboard.KeyboardDevice} to send key events (through
 * the {@link simulation.component.device.keyboard.KeyboardDevice#keyQueue} method.
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
   * Accept a new key press event. The k argument can be any character: it is converted (if
   * possible) to code page 437. Otherwise it gets ignored.
   *
   * @param k character of key pressed
   */
  void accept(char k) {
    // encode to code page 437
    CharsetEncoder encoder = Charset.forName("Cp437")
            .newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);

    CharBuffer cb = CharBuffer.wrap(new char[]{k});

    try {
      ByteBuffer bb = encoder.encode(cb);

      // insert key in listener queue
      listener.queueKey(bb.get());
    } catch (CharacterCodingException e) {
      // can't convert, ignore
    }

  }
}
