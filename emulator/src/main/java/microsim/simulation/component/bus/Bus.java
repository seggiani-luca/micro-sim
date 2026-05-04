package microsim.simulation.component.bus;

import microsim.simulation.Simulation;
import microsim.simulation.component.SimulationComponent;

/**
 * Represents a communication bus for a simulation. A bus is comprised of:
 * <ul>
 * <li>A 32 bit address line.</li>
 * <li>A 32 bit data line.</li>
 * <li>
 * Control lines (active high):
 * <ul>
 * <li>Read Enable line.</li>
 * <li>Write Enable line.</li>
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
 * 3-state logic.
 */
public class Bus extends SimulationComponent {

  /**
   * Byte select modes.
   */
  public class ByteSelect {

    /**
     * Hide constructor.
     */
    private ByteSelect() {
    }

    /**
     * Selects the first 8 bits of the data line.
     */
    public final static int BYTE = 0;

    /**
     * Selects the first 16 bits of the data line.
     */
    public final static int HALF = 1;

    /**
     * Selects the first 32 bits of the data line.
     */
    public final static int WORD = 2;
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
   * Byte select mode control line (thorugh {@link microsim.simulation.component.bus.Bus.ByteSelect}
   * enum).
   */
  public TSLine byteSelect;

  /**
   * Instantiates a bus by initializing address, data and control lines.
   *
   * @param simulation simulation this bus belongs to
   */
  public Bus(Simulation simulation) {
    // buses aren't mounted to buses
    super(null, simulation);

    // init lines
    addressLine = new TSLine(this, simulation);
    dataLine = new TSLine(this, simulation);
    readEnable = new TSLine(this, simulation);
    writeEnable = new TSLine(this, simulation);
    byteSelect = new TSLine(this, simulation);
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
    // step each line
    addressLine.step();
    dataLine.step();
    readEnable.step();
    writeEnable.step();
    byteSelect.step();

    // get current address and byte select
    int addr = addressLine.read();
    int byteSel = byteSelect.read();

    // check address alignment
    if (!checkAlignment(addr, byteSel)) {
      throw new RuntimeException("Unaligned memory access");
    }

    // check read/write enable conflict
    boolean readEnb = readEnable.read() == 1;
    boolean writeEnb = writeEnable.read() == 1;
    if (readEnb && writeEnb) {
      throw new RuntimeException("Read Enable and Write Enable simultaneously high");
    }
  }
}
