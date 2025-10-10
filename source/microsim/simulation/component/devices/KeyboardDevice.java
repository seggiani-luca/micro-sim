package microsim.simulation.component.devices;

import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JComponent;
import microsim.simulation.component.*;

public class KeyboardDevice extends IoDevice {

  Queue<Integer> keyQueue = new LinkedList<>();

  public void attachComponent(JComponent component) {
    component.setFocusable(true);
    component.requestFocusInWindow();
    component.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        synchronized (keyQueue) {
          keyQueue.add(e.getKeyCode());
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
