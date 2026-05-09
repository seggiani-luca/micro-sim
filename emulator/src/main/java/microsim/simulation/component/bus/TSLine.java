package microsim.simulation.component.bus;

import microsim.simulation.Simulation;
import microsim.simulation.component.BusComponent;

/**
 * Models a 3-state logic line. Offers 2 main features: one driver/multiple readers functionality,
 * and step-synced buffering for received data. Only int lines are defined for performance reasons.
 * Byte selection is done through an enum class. Booleans use the usual 0 = false, 1 = true
 * convention. Helpers are defined to handle booleans accordingly.
 */
public class TSLine extends BusComponent {

  /**
   * Component that is currently driving this line.
   */
  private BusComponent driver;

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
   * @param simulation name of simulation this bus belongs to
   */
  public TSLine(Bus bus, Simulation simulation) {
    super(bus, simulation);
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
  public void drive(BusComponent driver, int data) {
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
   * Boolean version of {@link #drive(microsim.simulation.component.BusComponent, int)}.
   *
   * @param driver component requesting to become driver
   * @param data data (as bool) to drive line with
   */
  public void driveBool(BusComponent driver, boolean data) {
    drive(driver, data ? 1 : 0);
  }

  /**
   * Used by drivers to release line. Null drivers and drivers who don't own the line cannot release
   * it. Releasing a line means doesn't clear it's {@link #bufferedData}. This means
   * {@link #committedData} floats: that is expected behavior.
   *
   * @param driver driver requesting release
   */
  public void release(BusComponent driver) {
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

  /**
   * Reads data as bool (commited)
   *
   * @return commited data on line, as bool
   */
  public boolean readBool() {
    return committedData == 1;
  }
}
