package microsim.simulation.component.device.timer;

import microsim.simulation.component.bus.*;
import microsim.simulation.component.device.ThreadedIoDevice;

/**
 * Implements a timer device that periodically raises a status line.
 */
public class TimerDevice extends ThreadedIoDevice {

  /**
   * Frequency of timer clock.
   */
  public static final int MASTER_FREQ = 1000; // in hz

  /**
   * Period of timer clock.
   */
  public static final long MASTER_TIME = 1_000_000_000 / MASTER_FREQ; // in ns

  /**
   * Signals if timer has ticked and hasn't been read yet.
   */
  private boolean ticked = false;

  /**
   * Instantiates timer device, taking a reference to the bus it's mounted on and the base address
   * it should respond from.
   *
   * @param bus bus the timer device is mounted on
   * @param base base address of timer device
   * @param simulationName name of the simulation this timer device belongs to
   */
  public TimerDevice(Bus bus, int base, String simulationName) {
    super(bus, simulationName, base, 1);
  }

  /**
   * Returns timer ports. Port 0 is status.
   *
   * @param index index of port
   * @return value port should return
   */
  @Override
  public int getPort(int index) {
    boolean lastTicked = ticked;
    ticked = false;
    return lastTicked ? 1 : 0;
  }

  /**
   * Sets ports (doesn't do anything for timer device).
   *
   * @param index not significant
   * @param data not significant
   */
  @Override
  public void setPort(int index, int data) {
    // nothing to do
  }

  /**
   * Implements the thread that refreshes the timer.
   */
  @Override
  protected void deviceThread() {
    long waitTime = System.nanoTime();
    while (running) {
      ticked = true;

      waitTime += MASTER_TIME;
      smartSpin(waitTime);
    }
  }
}
