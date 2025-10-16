package microsim.simulation.component.memory;

import microsim.simulation.info.SimulationInfo;
import microsim.simulation.*;
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
   * Memory space info this component implements.
   */
  SimulationInfo.MemoryInfo info;

  /**
   * Checks if given address is in memory bounds.
   *
   * @param addr address to check
   * @return signals if address is in bounds
   */
  public boolean inBounds(int addr) {
    if (addr >= info.epromStart && addr <= info.epromEnd) {
      return true;
    } else if (addr >= info.ramStart && addr <= info.ramEnd) {
      return true;
    } else if (addr >= info.vramStart && addr <= info.vramEnd) {
      return true;
    }

    return false;
  }

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
   * Instantiates memory space, taking a reference to the bus it's mounted on and configuration info
   * (including the EPROM data it should load in the EPROM region).
   *
   * @param bus bus the component is mounted on
   * @param info info to build memory space from
   */
  public MemorySpace(Bus bus, SimulationInfo.MemoryInfo info) {
    super(bus);
    this.info = info;

    // setup memory arrays
    eprom = new byte[info.epromEnd - info.epromStart + 1];
    ram = new byte[info.ramEnd - info.ramStart + 1];
    vram = new byte[info.vramEnd - info.vramStart + 1];

    // check if EPROM data fits
    if (info.epromData.length > eprom.length) {
      throw new RuntimeException("Given EPROM data doesn't fit in EPROM");
    }

    // load EPROM data
    System.arraycopy(info.epromData, 0, eprom, 0, info.epromData.length);
  }

  /**
   * Checks if an address is aligned to the word size specified by byteSelect.
   *
   * @param addr address to check
   * @param byteSelect word size
   * @return boolean that signals whether alignment is respected
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
   * If both {@link simulation.component.bus.Bus#readEnable} and
   * {@link simulation.component.bus.Bus#writeEnable} are high, raises an exception. If requested
   * address is out of bounds, expect it to be a I/O operation and ignore.
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

    if (!checkAlignment(addr, byteSelect)) {
      throw new RuntimeException("Unaligned memory access");
    }

    if (readEnable && writeEnable) {
      throw new RuntimeException("Read Enable and Write Enable simultaneously high");
    }

    if (readEnable) {
      // read operation
      int data = 0x0;

      if (DebugShell.active) {
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

      if (DebugShell.active) {
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

      if (DebugShell.active) {
        raiseEvent(new DebugEvent(this, "Memory saw write operation at addr "
                + DebugShell.int32ToString(addr) + " of data "
                + DebugShell.int32ToString(data)));
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
    if (addr >= info.epromStart && addr <= info.epromEnd) {
      return eprom[addr - info.epromStart];
    } else if (addr >= info.ramStart && addr <= info.ramEnd) {
      return ram[addr - info.ramStart];
    } else if (addr >= info.vramStart && addr <= info.vramEnd) {
      if (!info.allowVramReads && !debugMode) {
        throw new RuntimeException("VRAM reads are forbidden.");
      }
      return vram[addr - info.vramStart];
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
    if (addr >= info.epromStart && addr <= info.epromEnd) {
      if (!info.allowEpromWrites && !debugMode) {
        throw new RuntimeException("EPROM writes are forbidden.");
      }
      eprom[addr - info.epromStart] = data;
    } else if (addr >= info.ramStart && addr <= info.ramEnd) {
      ram[addr - info.ramStart] = data;
    } else if (addr >= info.vramStart && addr <= info.vramEnd) {
      vram[addr - info.vramStart] = data;
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
