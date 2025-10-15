package microsim.simulation.component.device.keyboard;

import java.awt.event.*;
import javax.swing.JComponent;

/**
 * Implements a keyboard source by listening to keyboard events on a {@link javax.swing.JComponent}.
 */
public class JComponentKeyboardSource extends KeyboardSource {

  /**
   * Creates a keyboard source and attaches a component to it.
   *
   * @param component the JComponent from which we should grab input
   */
  public JComponentKeyboardSource(JComponent component) {
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
}
