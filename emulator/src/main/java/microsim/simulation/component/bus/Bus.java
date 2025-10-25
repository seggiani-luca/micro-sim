package microsim.simulation.component.bus;

import microsim.simulation.component.SimulationComponent;

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
 * Lines are implemented by {@link microsim.simulation.component.bus.TSLine} objects, modeling
 * 3-state logic. Only int lines are defined for performance reasons, byte selection is done through
 * an enum class, while booleans use the usual 0 = false, 1 = true convention.
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
   * Byte select mode control line (thorugh {@link simulation.component.bus.Bus.ByteSelect} enum).
   */
  public TSLine byteSelect;

  /**
   * Instantiates a bus by initializing address, data and control lines.
   *
   * @param simulationName name of simulation this bus belongs to
   */
  public Bus(String simulationName) {
    // buses aren't mounted to buses
    super(null, simulationName);

    addressLine = new TSLine(this, simulationName);
    dataLine = new TSLine(this, simulationName);
    readEnable = new TSLine(this, simulationName);
    writeEnable = new TSLine(this, simulationName);
    byteSelect = new TSLine(this, simulationName);
  }

  /**
   * Checks if an address is aligned to the word size specified by byteSelect.
   *
   * @param addr address to check
   * @param byteSelect word size
   * @return signals whether alignment is respected
   */
  private boolean checkAlignment(int addr, int byteSelect) {
    switch (byteSelect) {
      case ByteSelect.WORD -> {
        return (addr & 0x3) == 0;
      }
      case ByteSelect.HALF -> {
        return (addr & 0x1) == 0;
      }
      case ByteSelect.BYTE -> {
        return true;
      }
    }
    return false;
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

    // perform checks
    int addr = addressLine.read();
    int byteSel = byteSelect.read();

    if (!checkAlignment(addr, byteSel)) {
      throw new RuntimeException("Unaligned memory access");
    }

    boolean readEnb = readEnable.read() == 1;
    boolean writeEnb = writeEnable.read() == 1;

    if (readEnb && writeEnb) {
      throw new RuntimeException("Read Enable and Write Enable simultaneously high");
    }
  }
}
