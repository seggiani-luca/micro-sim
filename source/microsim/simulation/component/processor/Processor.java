package microsim.simulation.component.processor;

import java.util.Deque;
import java.util.LinkedList;
import microsim.simulation.component.*;
import microsim.simulation.component.Bus.ByteSelect;
import microsim.simulation.component.processor.MicroOp.OpType;
import microsim.simulation.event.DebugEvent;

/**
 * A processor implementing the RISC-V rv32i ISA. This comprises basic memory movement, arithmetic
 * and logic operations (except MULs and DIvs), and basic branching and stack management. For more
 * info, see the @link
 * <a href="www.cs.sfu.ca/~ashriram/Courses/CS295/assets/notebooks/RISCV/RISCV_CARD.pdf">green
 * card</a>.
 */
public class Processor extends SimulationComponent {

  /**
   * Number of registers. Fixed to 32 according to ABI.
   */
  public static final int REGISTERS = 32;

  /**
   * The reset value of program counter {@link #pc}. This is set to EPROM_BEG from
   * {@link microsim.simulation.component.MemorySpace}.
   */
  private static final int RESET_INSTRUCTION_ADDRESS = MemorySpace.EPROM_BEG;

  /**
   * Program counter, separate from registers.
   */
  int pc;

  /**
   * Gets program counter, used for debugging.
   *
   * @return pc
   */
  public int getPc() {
    return pc;
  }

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
  private int[] registers = new int[REGISTERS];

  /**
   * Gets register at index, ensuring zero register behavior.
   *
   * @param i index of register to get
   * @return register value
   */
  int getRegister(int i) {
    if (i == 0) {
      return 0;
    } else {
      return registers[i];
    }
  }

  /**
   * Sets register at index, ensuring zero register behavior.
   *
   * @param i index of register to set
   * @param val value to set register to
   */
  void setRegister(int i, int val) {
    if (i == 0) {
      return;
    } else {
      registers[i] = val;
    }
  }

  /**
   * Returns all registers, used for debugging.
   *
   * @return all registers
   */
  public int[] getRegisters() {
    return registers;
  }

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

    // reset instruction pointer
    pc = RESET_INSTRUCTION_ADDRESS;
  }

  /**
   * Temporary value used to keep bus operation results in-between microops.
   */
  int temp;

  /**
   * Temporary indicator of read/write byteSelect.
   */
  ByteSelect byteSelect;

  /**
   * Queue of microops to execute, basically acts as a pipeline.
   */
  Deque<MicroOp> opQueue = new LinkedList<>();

  /**
   * Returns all current microops, used for debugging.
   *
   * @return current microops
   */
  public Deque<MicroOp> getMicroOps() {
    return opQueue;
  }

  /**
   * Sets processor up for a fetch execute cycle, called when microop queue is empty.
   */
  private void fetchDecode() {
    // read next instruction word and move
    BusInterface.doReadRoutine(this, pc, ByteSelect.WORD);

    // decode instruction word
    opQueue.add(new MicroOp(OpType.DECODE));
  }

  /**
   * Steps by fetching the next microop and executing it, or filling the queue with
   * {@link #fetchDecode} if it's empty
   */
  @Override
  public void step() {
    MicroOp nextOp = opQueue.poll();

    if (nextOp == null) {
      raiseEvent(new DebugEvent(this,
        "Processor found empty pipeline and started fetch-decode cycle"));
      fetchDecode();
    } else {
      raiseEvent(new DebugEvent(this,
        "Processor found microop " + nextOp.toString()));
      nextOp.execute(this);
    }
  }

}
