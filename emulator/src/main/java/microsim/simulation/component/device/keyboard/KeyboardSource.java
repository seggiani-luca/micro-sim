package microsim.simulation.component.device.keyboard;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import javax.swing.JComponent;

/**
 * Implements an input source that attaches to a
 * {@link microsim.simulation.component.device.keyboard.KeyboardDevice} to send key events.
 * Characters events are obtained by listening to keyboard events on a
 * {@link javax.swing.JComponent}.
 */
public class KeyboardSource {

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
   * Creates a keyboard source and attaches a component to it.
   *
   * @param component the JComponent from which we should grab input
   */
  public KeyboardSource(JComponent component) {
    // should have focus to grab input
    component.setFocusable(true);
    component.requestFocusInWindow();

    // tab needs to be caputred
    component.setFocusTraversalKeysEnabled(false);

    // add listener
    component.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        accept(e.getKeyChar());
      }
    });
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
      listener.key = bb.get();
    } catch (CharacterCodingException e) {
      // can't convert, ignore
    }

  }
}
