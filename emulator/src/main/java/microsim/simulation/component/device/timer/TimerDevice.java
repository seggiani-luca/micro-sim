package microsim.simulation.component.device.timer;

import microsim.simulation.component.bus.*;
import microsim.simulation.info.TimerInfo;
import microsim.simulation.component.device.ThreadedIoDevice;

/**
 *
 * @author luca
 */
public class TimerDevice extends ThreadedIoDevice {

  /**
   * Timer info this component implements.
   */
  TimerInfo info;

  /**
   * Period of timer clock.
   */
  public long masterTime;

  /**
   * Signals if timer has ticked and hasn't been read yet.
   */
  boolean ticked = false;

  /**
   * Instantiates timer device, taking a reference to the bus it's mounted on.
   *
   * @param bus bus the component is mounted on
   * @param info info to build video device from
   */
  public TimerDevice(Bus bus, TimerInfo info) {
    super(bus, info.base, 1);
    this.info = info;

    masterTime = 1_000_000_000 / info.masterFreq; // in ns
  }

  @Override
  protected void deviceThread() {
    long waitTime = System.nanoTime();
    while (true) {
      ticked = true;

      waitTime += masterTime;
      smartSpin(waitTime);
    }
  }

  @Override
  public int getPort(int index) {
    boolean lastTicked = ticked;
    ticked = false;
    return lastTicked ? 1 : 0;
  }

  @Override
  public void setPort(int index, int data) {
    // nothing to do
  }

}
