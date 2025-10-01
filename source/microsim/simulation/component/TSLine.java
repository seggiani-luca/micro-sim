package microsim.simulation.component;

/**
 * Models a 3-state logic line. Offers 2 main features: one driver/multiple readers functionality,
 * and step-synced buffering for received data.
 *
 * @param <T> type of data on line
 */
public class TSLine<T> extends SimulationComponent {

  /**
   * Component that is currently driving this line.
   */
  private SimulationComponent driver;

  /**
   * Committed data (visible from reads), gets {@link #bufferedData}'s value when component is
   * stepped.
   */
  private T committedData;

  /**
   * Buffered data, gets updated the moment {@link #driver} calls {@link #drive}.
   */
  private T bufferedData;

  /**
   * Propagates {@link #bufferedData} to {@link #committedData}.
   */
  @Override
  public void step() {
    committedData = bufferedData;
  }

  /**
   * Used by components to drive this line. Driver cannot be null, and cannot attempt to drive
   * already driven line.
   *
   * @param driver component requesting to become driver
   * @param data data to drive line with
   */
  public void drive(SimulationComponent driver, T data) {
    // driver can't be null
    if (driver == null) {
      throw new RuntimeException("Null driver cannot drive TSLine");
    }

    // trying to drive free line
    if (this.driver == null) {
      this.driver = driver;
      this.bufferedData = data;

      return;
    }

    // trying to drive already driven line
    if (driver != this.driver) {
      throw new RuntimeException(
        driver.getClass().getName()
        + " trying to drive TSLine already driven by "
        + this.driver.getClass().getName()
      );
    }

    // already driving line
    this.bufferedData = data;
  }

  /**
   * Used by drivers to release line. Null drivers and drivers who don't own the line cannot release
   * it.
   *
   * @param driver driver requesting release
   */
  public void release(SimulationComponent driver) {
    if (driver == null) {
      throw new RuntimeException("Null driver cannot release TSLine");
    }

    // trying to release line not owned
    if (driver != this.driver) {
      throw new RuntimeException(
        driver.getClass().getName()
        + " trying to release TSLine already driven by "
        + this.driver.getClass().getName()
      );
    }

    // release line
    this.driver = null;
    this.bufferedData = null;
  }

  /**
   * Reads data (committed).
   *
   * @return committed data on line
   */
  public T read() {
    return committedData;
  }
}
