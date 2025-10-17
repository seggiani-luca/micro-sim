package microsim.simulation.component.device.keyboard;

import java.util.LinkedList;
import java.util.Queue;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.device.IoDevice;
import microsim.simulation.info.KeyboardInfo;

/**
 * Implements a keyboard devices that mantains a character queue and returns them via a status and a
 * data port.
 */
public class KeyboardDevice extends IoDevice {

  /**
   * Keyboard info this device implements.
   */
  @SuppressWarnings("unused")
  KeyboardInfo info; // currently unused

  /**
   * Queue of characters pressed.
   */
  private Queue<Integer> keyQueue = new LinkedList<>();

  /**
   * Attaches keyboard device to a
   * {@link microsim.simulation.component.device.keyboard.KeyboardSource} to grab keyboard input
   * from.
   *
   * @param source keyboard input source to grab input from
   */
  public void attachSource(KeyboardSource source) {
    source.setListener(this);
  }

  /**
   * Instantiates keyboard device, taking a reference to the bus it's mounted on.
   *
   * @param bus bus the component is mounted on
   * @param info info to build this device from
   */
  public KeyboardDevice(Bus bus, KeyboardInfo info) {
    super(bus, info.base, 2);
    this.info = info;
  }

  /**
   * Queues a character codepoint on the key queue. This is meant to be called by a
   * {@link microsim.simulation.component.device.keyboard.KeyboardSource} implementation.
   *
   * @param k ASCII codepoint of key pressed
   */
  public void queueKey(byte k) {
    keyQueue.add(Integer.valueOf(k));
  }

  /**
   * Returns keyboard ports. Port 0 is status and port 1 is data.
   *
   * @param index index of port
   * @return value port should return
   */
  @Override
  public int getPort(int index) {
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
  public void setPort(int index, int data) {
    // nothing to set
    return;
  }
}
