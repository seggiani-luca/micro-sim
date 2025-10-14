package microsim.simulation.component.device.timer;

import microsim.simulation.component.bus.Bus;
import microsim.simulation.component.device.ThreadedIoDevice;

public class TimerDevice extends ThreadedIoDevice {

  public static final long MASTER_FREQ = 1_000; // 1 KHz
  public static final long MASTER_TIME = 1_000_000_000L / MASTER_FREQ;

  boolean ticked = false;

  public TimerDevice(Bus bus, int base) {
    super(bus, base, 1);
  }

  @Override
  protected void deviceThread() {
    long waitTime = System.nanoTime();
    while (true) {
      ticked = true;

      waitTime += MASTER_TIME;
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
