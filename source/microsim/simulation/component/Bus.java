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
 * logic. Only int lines are defined for performance reasons, byte selection is done through an enum
 * class, while booleans use the usual 0 = false, 1 = true convention.
 */
public class Bus extends SimulationComponent {

  /**
   * Byte select modes.
   */
  public static class ByteSelect {

    public static final int BYTE = 0; // 8 bits
    public static final int HALF = 1; // 16 bits
    public static final int WORD = 2; // 32 bits
  }

  /**
   * 32 bit address line.
   */
  public TSLine addressLine;
  /**
   * 32 bit data line.
   */
  public TSLine dataLine;

  /**
   * Read enable control line (boolean, active high).
   */
  public TSLine readEnable;

  /**
   * Write enable control line (boolean, active high).
   */
  public TSLine writeEnable;

  /**
   * Byte select mode control line (thorugh {@link microsim.simulation.component.Bus.ByteSelect}
   * enum).
   */
  public TSLine byteSelect;

  /**
   * Instantiates a bus by initializing address, data and control lines.
   */
  public Bus() {
    addressLine = new TSLine();
    dataLine = new TSLine();
    readEnable = new TSLine();
    writeEnable = new TSLine();
    byteSelect = new TSLine();
  }

  /**
   * Steps by updating the 3-state lines.
   */
  @Override
  public final void step() {
    addressLine.step();
    dataLine.step();
    readEnable.step();
    writeEnable.step();
    byteSelect.step();
  }
}
