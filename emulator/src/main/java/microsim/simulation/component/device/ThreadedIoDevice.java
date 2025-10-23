package microsim.simulation.component.device;

import java.util.concurrent.locks.LockSupport;
import microsim.simulation.component.bus.*;
import microsim.ui.DebugShell;

/**
 * Implements a device that should be simulated on a thread alongside the main simulation loop. This
 * is done via a Runnable subclass which gets instantiated by the constructor and kept private. This
 * class just hooks into the abstract method {@link #deviceThread()}. All the device implementations
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
   * Method that implements the actual device thread. To be implemented by subclass.
   */
  protected abstract void deviceThread();

  /**
   * Spins the thread until the given time, parking it if remaining time is over a millisecond.
   * Threads that want to obey the debugger's "stop the world" policy should use this method to
   * sleep.
   *
   * @param time time to wait to
   */
  protected void smartSpin(long time) {
    // sleep if world is stopped
    while (DebugShell.isWorldStopped()) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        throw new RuntimeException("Sleeping thread was interrupted while world was stopped.");
      }
    }

    // wait for time
    long now = System.nanoTime();
    long remaining = time - now;

    if (remaining > 1_000_000) { // over 1 ms
      LockSupport.parkNanos(remaining - 500_000);
    }
    while (System.nanoTime() < time) {
      // more granular wait
      Thread.onSpinWait();
    }
  }

  /**
   * Begins executing device thread. Takes a reference to the machine name of the simulations it
   * belongs to for organization.
   *
   * @param machineName machine name of simulation
   */
  public void begin(String machineName) {
    Thread deviceThread = new Thread(runnableInstance);
    deviceThread.setName("threaded device - " + this.getClass().getSimpleName() + " - "
            + machineName);
    deviceThread.start();
  }
}
