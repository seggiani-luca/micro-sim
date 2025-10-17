package microsim.simulation.component.processor;

import java.util.Deque;
import java.util.LinkedList;
import microsim.simulation.component.*;
import microsim.simulation.component.bus.*;
import microsim.simulation.event.*;
import microsim.simulation.info.ProcessorInfo;
import microsim.ui.DebugShell;

/**
 * A processor implementing the RISC-V rv32i ISA. This comprises basic memory movement, arithmetic
 * and logic operations (except MULs and DIvs), and basic branching and stack management. For more
 * info, see the @link
 * <a href="www.cs.sfu.ca/~ashriram/Courses/CS295/assets/notebooks/RISCV/RISCV_CARD.pdf">green
 * card</a>.
 */
public class Processor extends SimulationComponent {

  /**
   * Processor info this component implements.
   */
  @SuppressWarnings("unused")
  ProcessorInfo info; // currently unused

  /**
   * Number of registers. Fixed to 32 according to ABI.
   */
  public static final int REGISTERS = 32;

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
  int[] registers = new int[REGISTERS];

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
    registers[i] = val; // don't check, nobody uses it anyway
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
   * Instantiates processor, taking a reference to the bus it's mounted on and configuration info.
   * Resets instruction pointer to {@link #RESET_INSTRUCTION_ADDRESS}.
   *
   * @param bus bus the component is mounted on
   * @param info info to build processor from
   */
  @SuppressWarnings("LeakingThisInConstructor")
  public Processor(Bus bus, ProcessorInfo info) {
    super(bus);
    this.info = info;

    // processor  takes control of all lines but data and byteSelect
    // leaks this in constructor but we don't expect TSLine objects to do anything with it
    bus.addressLine.drive(this, 0);
    bus.readEnable.drive(this, 0);
    bus.writeEnable.drive(this, 0);

    // reset instruction pointer
    pc = info.resetInstructionAddress;
  }

  /**
   * Temporary value used to keep bus operation results in-between microops.
   */
  int temp;

  /**
   * Temporary indicator of read/write byteSelect.
   */
  int byteSelect;

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
    BusInterface.doReadRoutine(this, pc, Bus.ByteSelect.WORD);

    // decode instruction word
    opQueue.add(new MicroOp(MicroOp.OpType.DECODE));
  }

  /**
   * Steps by fetching the next microop and executing it, or filling the queue with
   * {@link #fetchDecode} if it's empty
   */
  @Override
  public final void step() {
    MicroOp nextOp = opQueue.poll();

    if (nextOp == null) {
      if (DebugShell.active) {
        raiseEvent(new DebugEvent(this,
                "Processor found empty pipeline and started fetch-decode cycle"));
      }
      fetchDecode();
    } else {
      if (DebugShell.active) {
        raiseEvent(new DebugEvent(this,
                "Processor found microop " + nextOp.toString()));
      }
      nextOp.execute(this);
    }
  }

}
