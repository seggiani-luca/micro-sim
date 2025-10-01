package microsim.simulation.component;

/**
 * Represents a communication bus for a simulation. A bus is comprised of:
 * <ul>
 * <li>A 16 bit address line.</li>
 * <li>A 16 bit data line.</li>
 * <li>
 * Control lines (active high):
 * <ul>
 * <li>Read enable line.</li>
 * <li>Write enable line.</li>
 * <li>Target space line. Low is memory, high is I/O space.</li>
 * </ul>
 * </li>
 * </ul>
 * Lines are implemented by {@link microsim.simulation.component.TSLine} objects, modeling 3-state
 * logic.
 */
public class Bus extends SimulationComponent {

  /**
   * 16 bit address line.
   */
  public TSLine<Character> addressLine;
  /**
   * 16 bit data line.
   */
  public TSLine<Character> dataLine;

  /**
   * Read enable control line (active high).
   */
  public TSLine<Boolean> readEnable;

  /**
   * Write enable control line (active high).
   */
  public TSLine<Boolean> writeEnable;

  /**
   * Target space control line. Low is memory, high is I/O space.
   */
  public TSLine<Boolean> targetSpace;

  /**
   * Instantiates a bus by initializing address, data and control lines.
   */
  public Bus() {
    addressLine = new TSLine<>();
    dataLine = new TSLine<>();

    readEnable = new TSLine<>();
    writeEnable = new TSLine<>();

    targetSpace = new TSLine<>();
  }

  /**
   * Steps by stepping the 3-state lines.
   */
  @Override
  public void step() {
    // stepping bus means stepping lines
    addressLine.step();
    dataLine.step();

    readEnable.step();
    writeEnable.step();

    targetSpace.step();
  }
}
