package microsim.simulation.component.memory;

import microsim.simulation.component.*;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.bus.Bus.ByteSelect;
import microsim.simulation.event.*;
import microsim.ui.DebugShell;

/**
 * Implements a memory space as a (to outside users) contiguous array of byte locations. Memory is
 * divided in three regions:
 * <ol>
 * <li>EPROM: contains program code and data at startup.</li>
 * <li>RAM: for general access.</li>
 * <li>VRAM: gets rendered to video on
 * {@link simulation.component.device.video.VideoRenderer#render()} calls.</li>
 * </ol>
 * Regions are defined by begin/end address pairs. End addresses are inclusive (0x000 to 0x0ff means
 * 0x0ff is in the region and 0x100 isn't).
 */
public class MemorySpace extends SimulationComponent {

  /**
   * Beginning of EPROM region.
   */
  public static final int EPROM_START = 0x00000000;

  /**
   * End of EPROM region.
   */
  public static final int EPROM_END = 0x0000ffff;
  /**
   * Beginning of RAM region.
   */
  public static final int RAM_START = 0x00010000;

  /**
   * End of RAM region.
   */
  public static final int RAM_END = 0x0001ffff;

  /**
   * Beginning of VRAM region.
   */
  public static final int VRAM_START = 0x00020000;

  /**
   * End of VRAM region.
   */
  public static final int VRAM_END = 0x0002ffff;

  /**
   * Should EPROM writes be allowed?
   */
  public static final boolean ALLOW_EPROM_WRITES = false;

  /**
   * Should EPROM reads be allowed?
   */
  public static final boolean ALLOW_VRAM_READS = true;

  /**
   * Holds EPROM data.
   */
  private final byte[] eprom;

  /**
   * Holds RAM data.
   */
  private final byte[] ram;

  /**
   * Holds VRAM data.
   */
  private final byte[] vram;

  /**
   * Signals that the memory space is driving the bus, and should release it at the next simulation
   * step.
   */
  private boolean driving;

  /**
   * Instantiates memory space, taking a reference to the bus it's mounted on.
   *
   * @param bus bus the memory space is mounted on
   * @param simulationName name of simulation this memory space belongs to
   */
  public MemorySpace(Bus bus, String simulationName) {
    super(bus, simulationName);

    // setup memory arrays
    eprom = new byte[EPROM_END - EPROM_START + 1];
    ram = new byte[RAM_END - RAM_START + 1];
    vram = new byte[VRAM_END - VRAM_START + 1];
  }

  /**
   * Checks if given address is in memory space bounds.
   *
   * @param addr address to check
   * @return signals if address is in bounds
   */
  public boolean inBounds(int addr) {
    if (addr >= EPROM_START && addr <= EPROM_END) {
      return true;
    } else if (addr >= RAM_START && addr <= RAM_END) {
      return true;
    } else if (addr >= VRAM_START && addr <= VRAM_END) {
      return true;
    }

    return false;
  }

  /**
   * Loads EPROM data into memory.
   *
   * @param epromData data to load
   */
  public void loadEPROM(byte[] epromData) {
    // check if EPROM data fits
    if (epromData.length > eprom.length) {
      throw new RuntimeException("Given EPROM data doesn't fit in EPROM");
    }

    // load EPROM data
    System.arraycopy(epromData, 0, eprom, 0, epromData.length);
  }

  /**
   * Steps by handling read/write operations seen on bus. Bus protocol is the following:
   * <ul>
   * <li>
   * If {@link simulation.component.bus.Bus#readEnable} is high, start a read operation:
   * <ol>
   * <li>Read address from {@link simulation.component.bus.Bus#addressLine}.</li>
   * <li>Read data at address.</li>
   * <li>Drive {@link simulation.component.bus.Bus#dataLine} for 1 simulation step.</li>
   * </ol>
   * </li>
   * <li>
   * If {@link simulation.component.bus.Bus#writeEnable} is high, start a write operation:
   * <ol>
   * <li>Read address from {@link simulation.component.bus.Bus#addressLine} and data from
   * {@link simulation.component.bus.Bus#dataLine}.</li>
   * <li>Write data at address.</li>
   * </ol>
   * </li>
   * </ul>
   * If requested address is out of bounds, expect it to be a I/O operation and ignore.
   */
  @Override
  public final void step() {
    // read address lines to check if memory was queried
    int addr = bus.addressLine.read();
    if (!inBounds(addr)) {
      return;
    }

    // read control lines
    boolean readEnable = bus.readEnable.read() == 1;
    boolean writeEnable = bus.writeEnable.read() == 1;
    int byteSelect = bus.byteSelect.read();

    if (readEnable) {
      // read operation
      int data = 0x0;

      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new DebugEvent(this, "Memory saw read operation at addr "
                + DebugShell.int32ToString(addr)));
      }

      // get word in (max) four byte reads
      switch (byteSelect) {
        case ByteSelect.WORD:
          data |= (readMemory(addr + 3) & 0xff) << 24;
          data |= (readMemory(addr + 2) & 0xff) << 16;
        case ByteSelect.HALF:
          data |= (readMemory(addr + 1) & 0xff) << 8;
        case ByteSelect.BYTE:
          data |= (readMemory(addr) & 0xff);
      }

      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new DebugEvent(this, "Memory read operation gave data "
                + DebugShell.int32ToString(data)));
      }

      // drive data line with word
      bus.dataLine.drive(this, data);
      driving = true;

      return;
    }

    if (writeEnable) {
      // write operation
      int data = bus.dataLine.read();

      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new DebugEvent(this, "Memory saw write operation at addr "
                + DebugShell.int32ToString(addr) + " of data " + DebugShell.int32ToString(data)));
      }

      // set word in (max) four byte writes
      switch (byteSelect) {
        case ByteSelect.WORD:
          writeMemory(addr + 3, (byte) (data >>> 24));
          writeMemory(addr + 2, (byte) (data >>> 16));
        case ByteSelect.HALF:
          writeMemory(addr + 1, (byte) (data >>> 8));
        case ByteSelect.BYTE:
          writeMemory(addr, (byte) data);
      }

      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new DebugEvent(this, "Memory write operation finished"));
      }

      return;
    }

    // release if driving
    if (driving) {
      bus.dataLine.release(this);
      driving = false;
    }
  }

  /**
   * Reads from memory space at a given address within simulation bounds (nothing changes but the
   * method is kept for symmetry). Implementation is done encapsulating
   * {@link #readMemory(int, boolean)} with debugMode always false.
   *
   * @param addr address to read from
   * @return data read
   */
  private byte readMemory(int addr) {
    return readMemory(addr, false);
  }

  /**
   * Reads from memory space at a given address. The debugMode flag specifies if forbidden behavior
   * should be enforced: the {@link microsim.ui.DebugShell} class uses it to allow debug operations.
   *
   * @param addr address to read from
   * @param debugMode enforce simulation correctness
   * @return data read
   */
  public byte readMemory(int addr, boolean debugMode) {
    if (addr >= EPROM_START && addr <= EPROM_END) {
      return eprom[addr - EPROM_START];
    } else if (addr >= RAM_START && addr <= RAM_END) {
      return ram[addr - RAM_START];
    } else if (addr >= VRAM_START && addr <= VRAM_END) {
      if (!ALLOW_VRAM_READS && !debugMode) {
        throw new RuntimeException("VRAM reads are forbidden.");
      }
      return vram[addr - VRAM_START];
    }

    return 0; // never reached
  }

  /**
   * Writes to memory space at a given address within simulation bounds. This means EPROM reads are
   * forbidden. Implementation is done encapsulating {@link #readMemory(int, boolean)} with
   * debugMode always false.
   *
   * @param addr address to write to
   * @param data data to write
   */
  private void writeMemory(int addr, byte data) {
    writeMemory(addr, data, false);
  }

  /**
   * Writes to memory space at a given address. The debugMode flag specifies if forbidden behavior
   * should be enforced: usage is same as {@link #readMemory(int, boolean)}.
   *
   * @param addr address to write to
   * @param data data to write
   * @param debugMode enforce simulation correctness
   */
  public void writeMemory(int addr, byte data, boolean debugMode) {
    if (addr >= EPROM_START && addr <= EPROM_END) {
      if (!ALLOW_EPROM_WRITES && !debugMode) {
        throw new RuntimeException("EPROM writes are forbidden.");
      }
      eprom[addr - EPROM_START] = data;
    } else if (addr >= RAM_START && addr <= RAM_END) {
      ram[addr - RAM_START] = data;
    } else if (addr >= VRAM_START && addr <= VRAM_END) {
      vram[addr - VRAM_START] = data;
    }
  }

  /**
   * Returns VRAM byte array. Used by
   * {@link microsim.simulation.component.device.video.VideoRenderer} for direct VRAM accesses when
   * rendering to frame buffer.
   *
   * @return VRAM byte array
   */
  public byte[] getVRAM() {
    return vram;
  }
}
