package microsim.simulation.component.bus;

import microsim.simulation.component.SimulationComponent;

/**
 * Models a 3-state logic line. Offers 2 main features: one driver/multiple readers functionality,
 * and step-synced buffering for received data.
 */
public class TSLine extends SimulationComponent {

  /**
   * Component that is currently driving this line.
   */
  private SimulationComponent driver;

  /**
   * Committed data (visible from reads), gets {@link #bufferedData}'s value when component is
   * stepped.
   */
  public int committedData;

  /**
   * Buffered data, gets updated the moment {@link #driver} calls {@link #drive}.
   */
  public int bufferedData;

  /**
   * Constructs a TSLine taking a reference to the bus it makes up.
   *
   * @param bus bus this line is part of
   */
  public TSLine(Bus bus) {
    super(bus);
  }

  /**
   * Propagates {@link #bufferedData} to {@link #committedData}.
   */
  @Override
  public final void step() {
    committedData = bufferedData;
  }

  /**
   * Used by components to drive this line. Driver cannot be null, and cannot attempt to drive an
   * already driven line. Driving a line means setting its {@link #bufferedData}. This doesn't mean
   * {@link #committedData} will update: that will happen at the next line update.
   *
   * @param driver component requesting to become driver
   * @param data data to drive line with
   */
  public void drive(SimulationComponent driver, int data) {
    // driver can't be null
    if (driver == null) {
      throw new RuntimeException("Null driver cannot drive TSLine");
    }

    // trying to drive already driven line
    if (this.driver != null && driver != this.driver) {
      throw new RuntimeException(driver.getClass().getName()
              + " trying to drive TSLine already driven by " + this.driver.getClass().getName());
    }

    this.driver = driver; // reassert if already driving
    this.bufferedData = data;
  }

  /**
   * Used by drivers to release line. Null drivers and drivers who don't own the line cannot release
   * it. Releasing a line means setting {@link #bufferedData} to null. This means
   * {@link #committedData} floats for one cycle: that is expected behavior.
   *
   * @param driver driver requesting release
   */
  public void release(SimulationComponent driver) {
    if (driver == null) {
      throw new RuntimeException("Null driver cannot release TSLine");
    }

    // trying to release line not owned
    if (driver != this.driver) {
      throw new RuntimeException(driver.getClass().getName()
              + " trying to release TSLine already driven by " + this.driver.getClass().getName());
    }

    // release line
    this.driver = null;
  }

  /**
   * Reads data (committed).
   *
   * @return committed data on line
   */
  public int read() {
    return committedData;
  }
}
