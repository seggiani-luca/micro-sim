package microsim.simulation.component.device;

import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JComponent;
import microsim.simulation.component.*;

/**
 * Implements a keyboard devices that mantains a character queue and returns them via a status and a
 * data port.
 */
public class KeyboardDevice extends IoDevice {

  /**
   * Queue of characters pressed.
   */
  Queue<Integer> keyQueue = new LinkedList<>();

  /**
   * Attaches keyboard device to a JComponent to grab keyboard input from.
   *
   * @param component JComponent to grab input from
   */
  public void attachComponent(JComponent component) {
    // should have focus to grab input
    component.setFocusable(true);
    component.requestFocusInWindow();

    // tab needs to be captured
    component.setFocusTraversalKeysEnabled(false);

    component.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        synchronized (keyQueue) {
          queueKey(e);
        }
      }
    });
  }

  /**
   * Instantiates keyboard device, taking a reference to the bus it's mounted on.
   *
   * @param bus bus the component is mounted on
   */
  public KeyboardDevice(Bus bus) {
    super(bus, 0x00040000, 2);
  }

  /**
   * Queues a character codepoint on the key queue, making sure it's a simple ASCII character.
   *
   * @param e event of key pressed
   */
  private void queueKey(KeyEvent e) {
    int keyChar = e.getKeyChar();

    // filter non basic ASCII characters
    if (keyChar == (keyChar & 0x7f)) {
      keyQueue.add(keyChar);
      return;
    }
  }

  /**
   * Returns keyboard ports. Port 0 is status and port 1 is data.
   *
   * @param index index of port
   * @return value port should return
   */
  @Override
  int getPort(int index) {
    switch (index) {
      case 0 -> {
        return keyQueue.isEmpty() ? 0 : 1;
      }
      case 1 -> {
        return keyQueue.isEmpty() ? 0 : keyQueue.poll();
      }
    }

    return 0;
  }

  /**
   * Sets port at index (doesn't do anything for keyboard device).
   *
   * @param index not significant
   * @param data not significant
   */
  @Override
  void setPort(int index, int data) {
    // nothing to set
    return;
  }
}
