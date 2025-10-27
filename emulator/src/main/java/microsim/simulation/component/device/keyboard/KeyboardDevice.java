package microsim.simulation.component.device.keyboard;

import java.util.LinkedList;
import java.util.Queue;
import microsim.simulation.Simulation;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.device.IoDevice;

/**
 * Implements a keyboard device that returns characters via a status and a data port.
 */
public class KeyboardDevice extends IoDevice {

  /**
   * Last key pressed.
   */
  byte key = '\0';

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
   * @param bus bus the keyboard device is mounted on
   * @param base base address of keyboard device
   * @param simulation simulation this keyboard device belongs to
   */
  public KeyboardDevice(Bus bus, int base, Simulation simulation) {
    super(bus, simulation, base, 2);
  }

  /**
   * Gets keyboard ports. Port 0 is status and port 1 is data.
   *
   * @param index index of port
   * @return value port should return
   */
  @Override
  @SuppressWarnings("null")
  public int getPort(int index) {
    switch (index) {
      case 0 -> {
        return (key == '\0') ? 0 : 1;
      }
      case 1 -> {
        byte prevKey = key;
        key = '\0';
        return prevKey;
      }
    }

    return 0;
  }

  /**
   * Sets ports (doesn't do anything for keyboard device).
   *
   * @param index not significant
   * @param data not significant
   */
  @Override
  public void setPort(int index, int data) {
    // nothing to set
  }
}
