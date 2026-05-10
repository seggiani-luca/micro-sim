package microsim.simulation.component.device.block;

import microsim.simulation.Simulation;
import microsim.simulation.component.bus.Bus;
import microsim.simulation.component.device.IoDevice;

/**
 * Implements a block/disk device, based on Parallel ATA, with 28 bit LBA addressing of 512 byte
 * sectors. Device ports are:
 * <ul>
 * <li>Data: 2 bytes;</li>
 * <li>Error: not significant for simulated device;</li>
 * <li>Address: 4 bytes (for 28 bit LBA plus HND);</li>
 * <li>Block counter;</li>
 * <li>Status / command: acts as status on reads and command on writes.</li>
 * </ul>
 */
public class BlockDevice extends IoDevice {

  /**
   * Size of block device (in bytes), fixed at 16 MiB.
   */
  public static final int STORAGE_SIZE = 16 * 1024 * 1024;

  /**
   * Size of block (in bytes).
   */
  public static final int BLOCK_SIZE = 512;

  /**
   * Block device storage byte array.
   */
  private final byte[] storage = new byte[STORAGE_SIZE];

  /**
   * Returns storage byte array. Used for disk image syncing.
   *
   * @return storage byte array
   */
  public byte[] getStorage() {
    return storage;
  }

  /**
   * Command to begin reading from disk.
   */
  public static final int READ_COMMAND = 0x00;

  /**
   * Command to begin writing to disk.
   */
  public static final int WRITE_COMMAND = 0x01;

  /**
   * Enum for disk operation types.
   */
  private static enum DiskOpType {
    /**
     * Read operation.
     */
    READ,
    /**
     * Write operation
     */
    WRITE
  }

  /**
   * Represents error state.
   */
  private boolean error = false;

  /**
   * Represents a single disk operation.
   */
  private static class DiskOp {

    /**
     * Default constructor.
     */
    public DiskOp() {
    }

    /**
     * Block address of operation.
     */
    private int blockAddress;


    /**
     * Number of blocks the operation affects.
     */
    private int blockNumber;

    /**
     * Type of operation.
     */
    private DiskOpType type;

    /**
     * The index of the next byte to read in storage.
     */
    private int byteIndex = 0;
  }

  /**
   * Current operation being executed by disk. Null means no operation is being executed.
   */
  private DiskOp currentOp = null;

  /**
   * Next operation to be executed by disk.
   */
  private DiskOp nextOp = new DiskOp();

  /**
   * Gets index of next byte pair to read. Shouldn't be called if currentOp is null.
   *
   * @return
   */
  private int getIndex() {
    // store previous index
    int ret = currentOp.byteIndex + currentOp.blockAddress * BLOCK_SIZE;

    // increment by 2 bytes
    currentOp.byteIndex += 2;

    // check if operation was completed
    if (currentOp.byteIndex >= BLOCK_SIZE * currentOp.blockNumber) {
      currentOp = null;
    }

    return ret;
  }

  /**
   * Validates nextOp and begins it if possible.
   */
  private void beginOperation() {
    // check that no operation was already being executed
    if (currentOp != null) {
      error = true;
      return;
    }

    // validate operation bounds
    long base = (long) nextOp.blockAddress * BLOCK_SIZE;
    long max = base + (long) nextOp.blockNumber * BLOCK_SIZE;
    if (base < 0 || max > STORAGE_SIZE || max <= base) {
      error = true;
      return;
    }

    // begin operation by inserting it into current operation
    currentOp = nextOp;
    nextOp = new DiskOp();
  }

  /**
   * Loads data into block device storage.
   *
   * @param data data to load
   */
  public void loadDisk(byte[] data) {
    // check if data fits
    if (data.length > storage.length) {
      throw new RuntimeException("Given storage file doesn't fit in block device");
    }

    // load data
    System.arraycopy(data, 0, storage, 0, data.length);
  }

  /**
   * Instantiates block device, taking a reference to the bus it's mounted on.
   *
   * @param bus bus the block device is mounted on
   * @param base base address of block device
   * @param simulation simulation this block device belongs to
   */
  public BlockDevice(Bus bus, int base, Simulation simulation) {
    super(bus, simulation, base, 5);
  }

  /**
   * Gets block device ports.
   *
   * @param index index of port
   * @return value port should return
   */
  @Override
  public int getPort(int index) {
    switch (index) {
      case 0 -> {
        // data port
        if (currentOp != null && currentOp.type == DiskOpType.READ) {
          int byteIndex = getIndex();
          int data = (storage[byteIndex + 1] & 0xff) << 8;
          data |= (storage[byteIndex] & 0xff);

          return data;
        }
      }
      case 1 -> {
        // error port
        int lastError = error ? 1 : 0;
        error = false;
        return lastError;
      }
      case 4 -> {
        // status port
        return (currentOp != null) ? 1 : 0;
      }
    }

    return 0;
  }

  /**
   * Sets block device ports.
   *
   * @param index index of port
   * @param data value to give port
   */
  @Override
  public void setPort(int index, int data) {
    switch (index) {
      case 0 -> {
        // data port
        if (currentOp != null && currentOp.type == DiskOpType.WRITE) {
          int byteIndex = getIndex();
          storage[byteIndex] = (byte) data;
          storage[byteIndex + 1] = (byte) (data >>> 8);
        }
      }
      case 2 -> {
        // address port
        int address = data & 0x0fffffff; // trim to 28 bit LBA
        nextOp.blockAddress = address;
      }
      case 3 -> {
        // block counter port
        nextOp.blockNumber = data & 0xff;
        if (data == 0) {
          nextOp.blockNumber = 256;
        }
      }
      case 4 -> {
        // command port, parse given command
        switch (data) {
          case READ_COMMAND -> {
            nextOp.type = DiskOpType.READ;
          }
          case WRITE_COMMAND -> {
            nextOp.type = DiskOpType.WRITE;
          }
          default -> {
            // ignore unknown commands
            return;
          }
        }

        // start operation if free
        beginOperation();
      }
    }
  }
}
