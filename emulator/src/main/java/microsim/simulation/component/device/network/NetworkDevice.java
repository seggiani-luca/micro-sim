package microsim.simulation.component.device.network;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import microsim.simulation.Simulation;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.device.IoDevice;

/**
 * Implements a network device that offers communication, through a hub, of words (32 bit) to all
 * other devices. Ports are same as an UART serial device:
 * <ol>
 * <li>TX (transmit buffer)</li>
 * <li>TXRDY (transmit buffer ready, that is empty)</li>
 * <li>RX (receive buffer)</li>
 * <li>RXRDY (receive buffer read, that is full)</li>
 * <li>ADDR (returns address of this device)</li>
 * </ol>
 * All buffers are 32 bit.
 */
public class NetworkDevice extends IoDevice {

  public static final int BUF_CAPACITY = 4096;
  public static final int ADDR_POOL_SIZE = 100;

  /**
   * List of all network device instances that need to receive what this device sends.
   */
  private static final List<NetworkDevice> instances = new CopyOnWriteArrayList<>();

  /**
   * Buffer of words (32 bit) this device is receiving.
   */
  private final Queue<Integer> inBuf = new ArrayBlockingQueue<>(BUF_CAPACITY);

  /**
   * Random pool of addresses to select from.
   */
  private final static int[] addressPool;

  static {
    // initialize static address pool with integers 1 to ADDR_POOL_SIZE
    addressPool = new int[ADDR_POOL_SIZE];
    for (int i = 0; i < ADDR_POOL_SIZE; i++) {
      addressPool[i] = i + 1;
    }

    // shuffle
    for (int src = 0; src < ADDR_POOL_SIZE; src++) {
      int dest = (int) (ADDR_POOL_SIZE * Math.random());
      int temp = addressPool[dest];
      addressPool[dest] = addressPool[src];
      addressPool[src] = temp;
    }
  }

  /**
   * Address of this network device.
   */
  private final int address;

  /**
   * Sends a data word (32 bit) to all network device instances.
   *
   * @param data data word to send
   */
  private void send(int data) {
    for (NetworkDevice net : instances) {
      if (net == this) {
        continue;
      }

      // offer, overflows get discarded
      net.inBuf.offer(data);
    }
  }

  /**
   * Instantiates network device, taking a reference to the bus it's mounted on.
   *
   * @param bus bus the network device is mounted on
   * @param base base address of network device
   * @param simulation simulation this network device belongs to
   */
  @SuppressWarnings("LeakingThisInConstructor")
  public NetworkDevice(Bus bus, int base, Simulation simulation) {
    super(bus, simulation, base, 5);
    instances.add(this);

    // get device address from pool
    address = addressPool[instances.size() - 1];
  }

  /**
   * Gets network ports. Ports are as follows:
   * <ol>
   * <li>TX</li>
   * <li>TXRDY</li>
   * <li>RX</li>
   * <li>RXRDY</li>
   * <li>ADDR</li>
   * </ol>
   *
   * @param index index of port
   * @return value port should return
   */
  @Override
  @SuppressWarnings("null")
  public int getPort(int index) {
    switch (index) {
      case 0 -> {
        // shouldn't read from transmit buffer
        return 0;
      }
      case 1 -> {
        // tx ready (empty)
        return 1;
      }
      case 2 -> {
        // rx
        Integer dat = inBuf.poll();
        return dat == null ? 0 : dat;
      }
      case 3 -> {
        // rx ready (full)
        return inBuf.peek() == null ? 0 : 1;
      }
      case 4 -> {
        // address
        return address;
      }
    }

    return 0;
  }

  /**
   * Sets network ports.
   * <ol>
   * <li>TX</li>
   * <li>TXRDY</li>
   * <li>RX</li>
   * <li>RXRDY</li>
   * <li>ADDR</li>
   * </ol>
   *
   * @param index not significant
   * @param data not significant
   */
  @Override
  public void setPort(int index, int data) {
    switch (index) {
      case 0 -> {
        // tx
        send(data);
      }
      case 1 -> {
        // shouldn't set tx ready
      }
      case 2 -> {
        // shouldn't write to read buffer
      }
      case 3 -> {
        // shouldn't set rx ready
      }
      case 4 -> {
        // shouldn't set address
      }
    }
  }
}
