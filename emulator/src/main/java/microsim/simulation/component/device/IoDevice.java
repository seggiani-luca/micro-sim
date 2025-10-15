package microsim.simulation.component.device;

import microsim.simulation.component.SimulationComponent;
import microsim.simulation.component.bus.Bus;

/**
 * Implements a device that exposes ports on the bus from a certain address. Memory and devices are
 * expected to share the same addressing space.
 */
public abstract class IoDevice extends SimulationComponent {

  /**
   * Base address of IO device.
   */
  private final int base;

  /**
   * Number of ports mapped from base address.
   */
  private final int ports;

  /**
   * Reference to the communication bus the component is mounted on.
   */
  private final Bus bus;

  /**
   * Signals that the device is driving the bus, and should release it at the next simulation step.
   */
  private boolean driving;

  /**
   * Instantiates device, taking a reference to the bus it's mounted on, the base address it should
   * respond from and the number of ports it offers.
   *
   * @param bus bus the component is mounted on
   * @param base base address
   * @param ports number of ports
   */
  public IoDevice(Bus bus, int base, int ports) {
    this.bus = bus;
    this.base = base;
    this.ports = ports;
  }

  /**
   * Checks if given address is in device space bounds.
   *
   * @param addr address to check
   * @return signals if address is in bounds
   */
  private boolean inBounds(int addr) {
    return addr >= base && addr < base + ports;
  }

  /**
   * Gets port at index.
   *
   * @param index index of port
   * @return value port should return
   */
  public abstract int getPort(int index);

  /**
   * Sets port at index.
   *
   * @param index index of port
   * @param data value to give port
   */
  public abstract void setPort(int index, int data);

  /**
   * Steps by handling port read/write operations seen on bus and calling {@link #getPort(int)} and
   * {@link #setPort(int, int)}.
   */
  @Override
  public final void step() {
    // read address lines to check if device was queried
    int addr = bus.addressLine.read();
    if (!inBounds(addr)) {
      return;
    }

    // is in bounds, get port index
    int portIdx = addr - base;

    // read control lines
    boolean readEnable = bus.readEnable.read() == 1;
    boolean writeEnable = bus.writeEnable.read() == 1;

    if (readEnable) {
      bus.dataLine.drive(this, getPort(portIdx));
      driving = true;
    }

    if (writeEnable) {
      int data = bus.dataLine.read();
      setPort(portIdx, data);
    }

    // release if driving
    if (driving) {
      bus.dataLine.release(this);
      driving = false;
    }
  }

}
