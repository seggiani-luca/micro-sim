package microsim.simulation.component.device;

import microsim.simulation.component.SimulationComponent;
import microsim.simulation.component.bus.*;
import microsim.simulation.event.DebugEvent;
import microsim.ui.DebugShell;

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
   * Signals that the device is driving the bus, and should release it at the next simulation step.
   */
  private boolean driving;

  /**
   * Instantiates device, taking a reference to the bus it's mounted on, the base address it should
   * respond from and the number of ports it offers.
   *
   * @param bus bus the device is mounted on
   * @param simulationName name of the simulation this device belongs to
   * @param base base address
   * @param ports number of ports
   */
  public IoDevice(Bus bus, String simulationName, int base, int ports) {
    super(bus, simulationName);
    this.base = base;
    this.ports = ports;
  }

  /**
   * Utility for returning device name through reflection.
   *
   * @return name of this device
   */
  protected String getDeviceName() {
    return this.getClass().getSimpleName();
  }

  /**
   * Checks if given address is in device space bounds.
   *
   * @param addr address to check
   * @return signals if address is in bounds
   */
  private boolean inBounds(int addr) {
    return addr >= base && addr < base + ports * 4;
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
    int portIdx = (addr - base) / 4;

    // read control lines
    boolean readEnable = bus.readEnable.read() == 1;
    boolean writeEnable = bus.writeEnable.read() == 1;

    if (readEnable) {
      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new DebugEvent(this, "Device " + getDeviceName() + " saw read operation at addr "
                + DebugShell.int32ToString(addr)));
      }

      // read port
      int portValue = getPort(portIdx);
      bus.dataLine.drive(this, portValue);

      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new DebugEvent(this, "Device " + getDeviceName() + " read operation gave data "
                + DebugShell.int32ToString(portValue)));
      }

      driving = true;
    }

    if (writeEnable) {
      // get data
      int data = bus.dataLine.read();

      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new DebugEvent(this, "Device " + getDeviceName()
                + " saw write operation at addr " + DebugShell.int32ToString(addr) + " of data "
                + DebugShell.int32ToString(data)));
      }

      // write port
      setPort(portIdx, data);

      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new DebugEvent(this, "Device " + getDeviceName() + " write operation finished"));
      }

    }

    // release if driving
    if (driving) {
      bus.dataLine.release(this);
      driving = false;
    }
  }

}
