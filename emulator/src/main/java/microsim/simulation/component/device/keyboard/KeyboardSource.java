package microsim.simulation.component.device.keyboard;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.JComponent;

/**
 * Implements an input source that attaches to a
 * {@link microsim.simulation.component.device.keyboard.KeyboardDevice}. Key events are obtained by
 * listening on a {@link javax.swing.JComponent}, then they are translated to make/break codes via a
 * scan code mapping.
 */
public class KeyboardSource {

  /**
   * Device instance that listens to this source.
   */
  private KeyboardDevice listener;

  /**
   * Scan set to use for translation.
   */
  private final Map<Integer, Byte> set;

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
   * @param requestedSet scan set to use for translation
   */
  public KeyboardSource(JComponent component, Map<Integer, Byte> requestedSet) {
    // configure translation set
    this.set = requestedSet;

    // should have focus to grab input
    component.setFocusable(true);
    component.requestFocusInWindow();

    // tab needs to be caputred
    component.setFocusTraversalKeysEnabled(false);

    // set up key adapter
    KeyAdapter keyAdapter = new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        handleKeyEvent(e, set, false);
      }

      @Override
      public void keyReleased(KeyEvent e) {
        handleKeyEvent(e, set, true);
      }
    };

    // add listener
    component.addKeyListener(keyAdapter);
  }

  /**
   * US to set 1 scan code translation.
   */
  public static final Map<Integer, Byte> set1 = Map.ofEntries(
          // letters
          Map.entry(KeyEvent.VK_A, (byte) 0x1E),
          Map.entry(KeyEvent.VK_B, (byte) 0x30),
          Map.entry(KeyEvent.VK_C, (byte) 0x2E),
          Map.entry(KeyEvent.VK_D, (byte) 0x20),
          Map.entry(KeyEvent.VK_E, (byte) 0x12),
          Map.entry(KeyEvent.VK_F, (byte) 0x21),
          Map.entry(KeyEvent.VK_G, (byte) 0x22),
          Map.entry(KeyEvent.VK_H, (byte) 0x23),
          Map.entry(KeyEvent.VK_I, (byte) 0x17),
          Map.entry(KeyEvent.VK_J, (byte) 0x24),
          Map.entry(KeyEvent.VK_K, (byte) 0x25),
          Map.entry(KeyEvent.VK_L, (byte) 0x26),
          Map.entry(KeyEvent.VK_M, (byte) 0x32),
          Map.entry(KeyEvent.VK_N, (byte) 0x31),
          Map.entry(KeyEvent.VK_O, (byte) 0x18),
          Map.entry(KeyEvent.VK_P, (byte) 0x19),
          Map.entry(KeyEvent.VK_Q, (byte) 0x10),
          Map.entry(KeyEvent.VK_R, (byte) 0x13),
          Map.entry(KeyEvent.VK_S, (byte) 0x1F),
          Map.entry(KeyEvent.VK_T, (byte) 0x14),
          Map.entry(KeyEvent.VK_U, (byte) 0x16),
          Map.entry(KeyEvent.VK_V, (byte) 0x2F),
          Map.entry(KeyEvent.VK_W, (byte) 0x11),
          Map.entry(KeyEvent.VK_X, (byte) 0x2D),
          Map.entry(KeyEvent.VK_Y, (byte) 0x15),
          Map.entry(KeyEvent.VK_Z, (byte) 0x2C),
          // numbers
          Map.entry(KeyEvent.VK_1, (byte) 0x02),
          Map.entry(KeyEvent.VK_2, (byte) 0x03),
          Map.entry(KeyEvent.VK_3, (byte) 0x04),
          Map.entry(KeyEvent.VK_4, (byte) 0x05),
          Map.entry(KeyEvent.VK_5, (byte) 0x06),
          Map.entry(KeyEvent.VK_6, (byte) 0x07),
          Map.entry(KeyEvent.VK_7, (byte) 0x08),
          Map.entry(KeyEvent.VK_8, (byte) 0x09),
          Map.entry(KeyEvent.VK_9, (byte) 0x0A),
          Map.entry(KeyEvent.VK_0, (byte) 0x0B),
          // whitespace
          Map.entry(KeyEvent.VK_ENTER, (byte) 0x1C),
          Map.entry(KeyEvent.VK_ESCAPE, (byte) 0x01),
          Map.entry(KeyEvent.VK_BACK_SPACE, (byte) 0x0E),
          Map.entry(KeyEvent.VK_TAB, (byte) 0x0F),
          Map.entry(KeyEvent.VK_SPACE, (byte) 0x39),
          // control
          Map.entry(KeyEvent.VK_SHIFT, (byte) 0x2A),
          Map.entry(KeyEvent.VK_CONTROL, (byte) 0x1D),
          Map.entry(KeyEvent.VK_ALT, (byte) 0x38),
          // symbols
          Map.entry(KeyEvent.VK_PLUS, (byte) 0x4E),
          Map.entry(KeyEvent.VK_MINUS, (byte) 0x0C),
          Map.entry(KeyEvent.VK_EQUALS, (byte) 0x0D),
          Map.entry(KeyEvent.VK_OPEN_BRACKET, (byte) 0x1A),
          Map.entry(KeyEvent.VK_CLOSE_BRACKET, (byte) 0x1B),
          Map.entry(KeyEvent.VK_BACK_SLASH, (byte) 0x2B),
          Map.entry(KeyEvent.VK_SEMICOLON, (byte) 0x27),
          Map.entry(KeyEvent.VK_QUOTE, (byte) 0x28),
          Map.entry(KeyEvent.VK_BACK_QUOTE, (byte) 0x29),
          Map.entry(KeyEvent.VK_COMMA, (byte) 0x33),
          Map.entry(KeyEvent.VK_PERIOD, (byte) 0x34),
          Map.entry(KeyEvent.VK_SLASH, (byte) 0x35)
  );

  /**
   * Handles key event by translating it to a make/break code and pushing it to the listener.
   *
   * @param e key event to handle
   * @param set set to get make/break translations from
   * @param breakCode should a break code be generated?
   */
  private void handleKeyEvent(KeyEvent e, Map<Integer, Byte> set, boolean breakCode) {
    // translate event to make/break code
    Byte code = set.get(e.getKeyCode());
    if (code == null) {
      return;
    }

    // make break code if needed
    if (breakCode) {
      code = (byte) (code | 0x80);
    }

    // push to listener
    listener.accept(code);
  }
}
