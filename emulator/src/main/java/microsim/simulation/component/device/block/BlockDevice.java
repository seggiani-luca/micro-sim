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
 * <li>Sector count;</li>
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
    super(bus, simulation, base, 2);
  }

  /**
   * Gets block device ports.
   *
   * @param index index of port
   * @return value port should return
   */
  @Override
  public int getPort(int index) {
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
  }
}
