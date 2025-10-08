package microsim.simulation.component;

/**
 * Represents a communication bus for a simulation. A bus is comprised of:
 * <ul>
 * <li>A 32 bit address line.</li>
 * <li>A 32 bit data line.</li>
 * <li>
 * Control lines (active high):
 * <ul>
 * <li>Read enable line.</li>
 * <li>Write enable line.</li>
 * <li>A byte select line with states:
 * <ol>
 * <li>Byte.</li>
 * <li>Half.</li>
 * <li>Word.</li>
 * </ol>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * Lines are implemented by {@link microsim.simulation.component.TSLine} objects, modeling 3-state
 * logic.
 */
public class Bus extends SimulationComponent {

  /**
   * Byte select modes.
   */
  public static enum ByteSelect {
    BYTE, // 8 bits
    HALF, // 16 bits
    WORD // 32 bits
  }

  /**
   * 32 bit address line.
   */
  public TSLine<Integer> addressLine;
  /**
   * 32 bit data line.
   */
  public TSLine<Integer> dataLine;

  /**
   * Read enable control line (active high).
   */
  public TSLine<Boolean> readEnable;

  /**
   * Write enable control line (active high).
   */
  public TSLine<Boolean> writeEnable;

  /**
   * Byte select mode control line.
   */
  public TSLine<ByteSelect> byteSelect;

  /**
   * Instantiates a bus by initializing address, data and control lines.
   */
  public Bus() {
    addressLine = new TSLine<>();
    dataLine = new TSLine<>();

    readEnable = new TSLine<>();
    writeEnable = new TSLine<>();
    byteSelect = new TSLine<>();
  }

  /**
   * Steps by updating the 3-state lines.
   */
  @Override
  public void step() {
    addressLine.step();
    dataLine.step();

    readEnable.step();
    writeEnable.step();
    byteSelect.step();
  }
}
