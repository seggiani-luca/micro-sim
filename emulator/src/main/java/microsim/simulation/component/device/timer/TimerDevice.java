package microsim.simulation.component.device.timer;

import microsim.simulation.Simulation;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.device.ThreadedIoDevice;

/**
 * Implements a programmable interval timer (PIT) with 3 channels. Device ports are:
 * <ul>
 * <li>Timer 0 gate;</li>
 * <li>Timer 1 gate;</li>
 * <li>Timer 2 gate;</li>
 * <li>Timer 0 configuration;</li>
 * <li>Timer 1 configuration;</li>
 * <li>Timer 2 configuration;</li>
 * </ul>
 * Timer configuration is done with a control word where the most significant bit is a periodic
 * flag, and the rest of the word is the max value of the counter.
 */
public class TimerDevice extends ThreadedIoDevice {

  /**
   * Number of timer channels.
   */
  public static final int NUM_CHANNELS = 3;


  /**
   * Frequency of timer clock.
   */
  public static final int MASTER_FREQ = 1000; // in hz

  /**
   * Period of timer clock.
   */
  public static final long MASTER_TIME = 1_000_000_000 / MASTER_FREQ; // in ns

  /**
   * Represents state of a timer channel.
   */
  private class TimerInfo {

    /**
     * Default constructor.
     */
    public TimerInfo() {
    }

    /**
     * Signals if the timer has ticked.
     */
    private boolean ticked = false;

    /**
     * Current timer counter.
     */
    private int counter = 0;

    /**
     * Max value of counter.
     */
    private int max = 0;

    /**
     * Should the timer be periodic?
     */
    private boolean periodic = false;
  }

  /**
   * Timer info for each channel.
   */
  private final TimerInfo[] timerInfos = new TimerInfo[NUM_CHANNELS];

  /**
   * Instantiates timer device, taking a reference to the bus it's mounted on and the base address
   * it should respond from.
   *
   * @param bus bus the timer device is mounted on
   * @param base base address of timer device
   * @param simulation simulation this timer device belongs to
   */
  public TimerDevice(Bus bus, int base, Simulation simulation) {
    super(bus, simulation, base, 2 * NUM_CHANNELS);

    // initialize timer infos
    for (int i = 0; i < NUM_CHANNELS; i++) {
      timerInfos[i] = new TimerInfo();
    }
  }

  /**
   * Returns timer ports. Port 0 is status.
   *
   * @param index index of port
   * @return value port should return
   */
  @Override
  public int getPort(int index) {
    // address in bounds
    if (index >= NUM_CHANNELS || index < 0) {
      return 0;
    }

    // get timer info
    TimerInfo info = timerInfos[index];

    // return if ticked
    synchronized (info) {
      if (info.ticked) {
        // if periodic, reset counter
        if (info.periodic) {
          info.counter = 0;
        }

        // reset
        info.ticked = false;
        return 1;
      }
    }

    return 0;
  }

  /**
   * Sets ports (doesn't do anything for timer device).
   *
   * @param index not significant
   * @param data not significant
   */
  @Override
  public void setPort(int index, int data) {
    // address in bounds (past input ports)
    index -= NUM_CHANNELS;
    if (index >= NUM_CHANNELS || index < 0) {
      return;
    }

    // get timer info
    TimerInfo info = timerInfos[index];

    synchronized (info) {
      // set up with given control word
      info.max = data & 0x7fffffff;
      info.periodic = (data & 0x80000000) != 0;

      // start timer
      info.ticked = false;
      info.counter = 0;
    }
  }

  /**
   * Implements the thread that refreshes the timer.
   */
  @Override
  protected void deviceThread() {
    while (running) {
      // increment each timer
      for (int i = 0; i < NUM_CHANNELS; i++) {
        // get timer info
        TimerInfo info = timerInfos[i];

        // increment
        synchronized (info) {
          if (info.counter < info.max) {
            info.counter++;

            // if reached max, tick
            if (info.counter == info.max) {
              info.ticked = true;
            }
          }
        }
      }

      // wait for master clock
      smartSpin(MASTER_TIME);
    }
  }
}
