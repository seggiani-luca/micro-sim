package microsim.simulation.component.device.keyboard;

import java.util.LinkedList;
import java.util.Queue;
import microsim.simulation.Simulation;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.device.IoDevice;

/**
 * Implements an IBM PC AT compliant keyboard device. Device ports are:
 * <ul>
 * <li>Status: 1 on available key code, 0 otherwise;</li>
 * <li>Data: next key make/break code.</li>
 * </ul>
 * Key make/break code mapping is based on IBM PS/2 PC scan sets, with translation performed by an
 * attached {@link microsim.simulation.component.device.keyboard.KeyboardSource}.
 */
public class KeyboardDevice extends IoDevice {

  /**
   * Queue of received key codes.
   */
  private final Queue<Byte> keyCodes = new LinkedList<>();

  /**
   * Pushes a key code to the key code queue.
   *
   * @param code key code to add
   */
  public void accept(Byte code) {
    keyCodes.add(code);
  }

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
  public int getPort(int index) {
    switch (index) {
      case 0 -> {
        // status port
        return keyCodes.isEmpty() ? 0 : 1;
      }
      case 1 -> {
        // data port
        return keyCodes.isEmpty() ? 0 : keyCodes.remove();
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
