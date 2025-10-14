package microsim.simulation.component.device;

import microsim.simulation.component.bus.Bus;

/**
 * Implements a device that should be simulated on a thread alongside the main simulation loop. This
 * is done via a Runnable subclass which gets instantiated by the constructor and kept private. This
 * class just hooks into the abstract method {@link #deviceThread()}. All device implementations
 * need to do is implement this method, and it will be run on its own thread.
 */
public abstract class ThreadedIoDevice extends IoDevice {

  /**
   * Runnable class that implements the device thread.
   */
  private class DeviceThread implements Runnable {

    /**
     * Run method should just hook into the subclass redefinition of {@link #deviceThread()}.
     */
    @Override
    public void run() {
      deviceThread();
    }
  }

  /**
   * The runnable instance the device thread executes.
   */
  private final Runnable runnableInstance;

  /**
   * Instantiates device, taking a reference to the bus it's mounted on, the base address it should
   * respond from and the number of ports it offers..
   *
   * @param bus bus the component is mounted on
   * @param base base address
   * @param ports number of ports
   */
  public ThreadedIoDevice(Bus bus, int base, int ports) {
    super(bus, base, ports);

    // set up runnable instance
    runnableInstance = new DeviceThread();
  }

  /**
   * Function that implements the actual device thread. To be implemented by subclass.
   */
  protected abstract void deviceThread();

  /**
   * Begins executing device thread.
   */
  public void begin() {
    new Thread(runnableInstance).start();
  }
}
