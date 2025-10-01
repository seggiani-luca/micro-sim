package microsim.simulation.component.processor;

import java.util.LinkedList;
import java.util.Queue;
import microsim.simulation.component.*;

public class Processor extends SimulationComponent {

  /**
   * Number of registers. Fixed to 32 according to ABI.
   */
  public static final int REGISTERS = 32;

  /**
   * The reset value of {@link #ip}. This is set to EPROM_BEG from
   * {@link microsim.simulation.component.MemorySpace}.
   */
  private static final int RESET_INSTRUCTION_ADDRESS = MemorySpace.EPROM_BEG;

  /**
   * Program counter, separate from registers.
   */
  int pc;

  /**
   * General registers. registers[0] is unused as it's the zero register. ABI specifies:
   * <table>
   * <tr>
   * <th>Register</th>
   * <th>ABI Name</th>
   * <th>Description</th>
   * </tr>
   * <tr>
   * <td>//</td>
   * <td>zero</td>
   * <td>Zero constant</td>
   * </tr>
   * <tr>
   * <td>registers[1]</td>
   * <td>ra</td>
   * <td>Return address</td>
   * </tr>
   * <tr>
   * <td>registers[2]</td>
   * <td>sp</td>
   * <td>Stack pointer</td>
   * </tr>
   * <tr>
   * <td>registers[3]</td>
   * <td>gp</td>
   * <td>Global pointer</td>
   * </tr>
   * <tr>
   * <td>registers[4]</td>
   * <td>tp</td>
   * <td>Thread pointer</td>
   * </tr>
   * <tr>
   * <td>registers[5-7]</td>
   * <td>t0-t2</td>
   * <td>Temporaries</td>
   * </tr>
   * <tr>
   * <td>registers[8]</td>
   * <td>s0 / fp</td>
   * <td>Saved register / frame pointer</td>
   * </tr>
   * <tr>
   * <td>registers[9]</td>
   * <td>s1</td>
   * <td>Saved register</td>
   * </tr>
   * <tr>
   * <td>registers[10-11]</td>
   * <td>a0-a1</td>
   * <td>Function arguments / return values</td>
   * </tr>
   * <tr>
   * <td>registers[12-17]</td>
   * <td>a2-a7</td>
   * <td>Function arguments</td>
   * </tr>
   * <tr>
   * <td>registers[18-27]</td>
   * <td>s2-s11</td>
   * <td>Saved registers</td>
   * </tr>
   * <tr>
   * <td>registers[28-31]</td>
   * <td>t3-t6</td>
   * <td>Temporaries</td>
   * </tr>
   * </table>
   */
  int[] registers = new int[REGISTERS];

  /**
   * Reference to the communication bus the component is mounted on.
   */
  final Bus bus;

  /**
   * Instantiates processor, taking a reference to the bus it's mounted on. Resets instruction
   * pointer to {@link #RESET_INSTRUCTION_ADDRESS}.
   *
   * @param bus bus the component is mounted on
   */
  public Processor(Bus bus) {
    this.bus = bus;

    // processor  takes control of all lines but data and byteSelect
    bus.addressLine.drive(this, 0);
    bus.readEnable.drive(this, false);
    bus.writeEnable.drive(this, false);
    bus.targetSpace.drive(this, false);

    // reset instruction pointer
    pc = RESET_INSTRUCTION_ADDRESS;

  }

  int temp;

  Queue<MicroOp> opQueue = new LinkedList<>();

  private void fetchDecode() {

  }

  void doReadOperation(int addr, Bus.BYTE_SELECT byteSelect) {
    bus.addressLine.drive(this, addr);
    bus.byteSelect.drive(this, byteSelect);
  }

  private void readRoutine() {
    opQueue.add(
      (cpu) -> {
        cpu.bus.readEnable.drive(this, true);
      }
    );
    opQueue.add(
      (cpu) -> {
        cpu.bus.readEnable.drive(this, false);
      }
    );
    opQueue.add(
      (cpu) -> {
        temp = cpu.bus.dataLine.read();
      }
    );
  }

  void doWriteOperation(int addr, int data, Bus.BYTE_SELECT byteSelect) {
    temp = data;
    bus.addressLine.drive(this, addr);
    bus.byteSelect.drive(this, byteSelect);
  }

  private void writeRoutine() {
    opQueue.add(
      (cpu) -> {
        cpu.bus.dataLine.drive(this, temp);
      }
    );
    opQueue.add(
      (cpu) -> {
        cpu.bus.writeEnable.drive(this, true);
      }
    );
    opQueue.add(
      (cpu) -> {
        cpu.bus.writeEnable.drive(this, false);
      }
    );
    opQueue.add(
      (cpu) -> {
        cpu.bus.dataLine.release(this);
      }
    );
  }

  @Override
  public void step() {
    MicroOp nextOp = opQueue.poll();

    if (nextOp == null) {
      fetchDecode();
    } else {
      nextOp.execute(this);
    }
  }

}
