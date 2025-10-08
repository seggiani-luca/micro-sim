package microsim.simulation.component;

import microsim.simulation.component.Bus.ByteSelect;
import microsim.simulation.event.*;
import microsim.ui.DebugShell;

/**
 * Implements a memory space as a (to outside users) contiguous array of byte locations. Memory is
 * divided in three regions:
 * <ol>
 * <li>EPROM: contains program code and data at startup.</li>
 * <li>RAM: for general access. </li>
 * <li>VRAM: gets rendered to video on {@link microsim.simulation.component.VideoDevice#render()}
 * calls.</li>
 * </ol>
 */
public class MemorySpace extends SimulationComponent {

  /**
   * Beginning of EPROM region.
   */
  public static final int EPROM_BEG = 0x0000_0000;

  /**
   * End of EPROM region.
   */
  public static final int EPROM_END = 0x0000_ffff;

  /**
   * Beginning of RAM region.
   */
  public static final int RAM_BEG = 0x0001_0000;

  /**
   * End of RAM region.
   */
  public static final int RAM_END = 0x00001_ffff;

  /**
   * Beginning of VRAM region.
   */
  public static final int VRAM_BEG = 0x0002_0000;

  /**
   * End of VRAM region.
   */
  public static final int VRAM_END = 0x0002_1400;

  /**
   * Checks if given address is in memory bounds.
   *
   * @param addr address to check
   * @return signals if address is in bounds
   */
  public static boolean inBounds(int addr) {
    if (addr >= EPROM_BEG && addr <= EPROM_END) {
      return true;
    } else if (addr >= RAM_BEG && addr <= RAM_END) {
      return true;
    } else if (addr >= VRAM_BEG && addr <= VRAM_END) {
      return true;
    }

    return false;
  }

  /**
   * Holds EPROM data;
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
   * Reference to the communication bus the component is mounted on.
   */
  private final Bus bus;

  /**
   * Signals that the memory space is driving the bus, and should release it at the next simulation
   * step.
   */
  private boolean driving;

  /**
   * Instantiates memory space, taking a reference to the bus it's mounted on and the EPROM data it
   * should load in the EPROM region.
   *
   * @param bus bus the component is mounted on
   * @param epromData EPROM data to load in {@link #eprom}
   */
  public MemorySpace(Bus bus, byte[] epromData) {
    this.bus = bus;

    // setup memory arrays
    eprom = new byte[EPROM_END - EPROM_BEG + 1];
    ram = new byte[RAM_END - RAM_BEG + 1];
    vram = new byte[VRAM_END - VRAM_BEG + 1];

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
   * <li>If {@link microsim.simulation.component.Bus#targetSpace} is not low, ignore any
   * operation.</li>
   * <li>
   * If {@link microsim.simulation.component.Bus#readEnable} is high, start a read operation:
   * <ol>
   * <li>Read address from {@link microsim.simulation.component.Bus#addressLine}.</li>
   * <li>Read data at address.</li>
   * <li>Drive {@link microsim.simulation.component.Bus#dataLine} for 1 simulation step.</li>
   * </ol>
   * </li>
   * <li>
   * If {@link microsim.simulation.component.Bus#writeEnable} is high, start a write operation:
   * <ol>
   * <li>Read address from {@link microsim.simulation.component.Bus#addressLine} and data from
   * {@link microsim.simulation.component.Bus#dataLine}.</li>
   * <li>Write data at address.</li>
   * </ol>
   * </li>
   * </ul>
   * If both {@link microsim.simulation.component.Bus#readEnable} and
   * {@link microsim.simulation.component.Bus#writeEnable} are high, raises an exception.
   */
  @Override
  public void step() {
    if (bus.targetSpace.read() != false) {
      // not targeting ram
      return;
    }

    // read control lines
    boolean readEnable = bus.readEnable.read();
    boolean writeEnable = bus.writeEnable.read();

    if (readEnable && writeEnable) {
      throw new RuntimeException("Read Enable and Write Enable simultaneously high");
    }

    if (readEnable) {
      // read operation
      int addr = bus.addressLine.read();
      ByteSelect byteSelect = bus.byteSelect.read();

      int data = 0x0;

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

      // rebuild word
      raiseEvent(new DebugEvent(this, "Memory saw read operation at addr "
        + DebugShell.int32ToString(addr) + " of data "
        + DebugShell.int32ToString(data)));

      // drive data line with word
      bus.dataLine.drive(this, data);
      driving = true;

      return;
    }

    if (writeEnable) {
      // write operation
      int addr = bus.addressLine.read();
      ByteSelect byteSelect = bus.byteSelect.read();
      int data = bus.dataLine.read();

      raiseEvent(new DebugEvent(this, "memory saw read operation at addr "
        + DebugShell.int32ToString(addr) + " of data "
        + DebugShell.int32ToString(data)));

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

      return;
    }

    // release if driving
    if (driving) {
      bus.dataLine.release(this);
      driving = false;
    }
  }

  /**
   * Reads from memory space at a given address. This is not meant to be used by other simulation
   * components, but by this class and {@link microsim.ui.DebugShell} objects displaying shells.
   *
   * @param addr address to read from
   * @return data read
   */
  public byte readMemory(int addr) {
    if (addr >= EPROM_BEG && addr <= EPROM_END) {
      return eprom[addr - EPROM_BEG];
    } else if (addr >= RAM_BEG && addr <= RAM_END) {
      return ram[addr - RAM_BEG];
    } else if (addr >= VRAM_BEG && addr <= VRAM_END) {
      // return vram[addr - VRAM_BEG];
      throw new RuntimeException("Memory read at VRAM");
    }

    throw new RuntimeException("Memory read out of bounds");
  }

  /**
   * Writes to memory space at a given address. Usage is same as {@link #readMemory(char)}.
   *
   * @param addr address to write to
   * @param data data to write
   */
  public void writeMemory(int addr, byte data) {
    if (addr >= EPROM_BEG && addr <= EPROM_END) {
      // eprom[addr - EPROM_BEG] = data;
      throw new RuntimeException("Memory write at EPROM");
    } else if (addr >= RAM_BEG && addr <= RAM_END) {
      ram[addr - RAM_BEG] = data;
      return;
    } else if (addr >= VRAM_BEG && addr <= VRAM_END) {
      vram[addr - VRAM_BEG] = data;
      return;
    }

    throw new RuntimeException("Memory write out of bounds");
  }

  /**
   * Returns VRAM byte array. Used by {@link microsim.simulation.component.VideoDevice} for direct
   * VRAM accesses when rendering to frame buffer.
   *
   * @return VRAM byte array
   */
  public byte[] getVRAM() {
    return vram;
  }
}
