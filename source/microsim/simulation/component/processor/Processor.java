package microsim.simulation.component.processor;

import java.util.Map;
import microsim.simulation.component.Bus;
import microsim.simulation.component.MemorySpace;
import microsim.simulation.component.SimulationComponent;
import microsim.simulation.event.*;
import microsim.ui.*;

/**
 * Implements a processor able to read/write on memory, load and execute instructions and govern I/O
 * devices. The processor is built on a state machine that updates on
 * {@link microsim.simulation.component.SimulationComponent#step()}.
 *
 * Available registers are:
 * <ul>
 * <li> {@link #GENERAL_REGISTERS} general registers. This is fixed to 4:
 * <ul>
 * <li>%a.</li>
 * <li>%b.</li>
 * <li>%c.</li>
 * <li>%d.</li>
 * </ul>
 * </li>
 * <li>
 * Special registers:
 * <ul>
 * <li>%ip, the instruction pointer. Resets to {@link #RESET_INSTRUCTION_ADDRESS}.</li>
 * <li>%sp, the stack pointer.</li>
 * <li>
 * Flag register, accessible only to jump instructions. It contains bits:
 * <ol start="0">
 * <li>of, overflow flag.</li>
 * <li>sf, sign flag.</li>
 * <li>zf, zero flag.</li>
 * <li>cf, carry flag.</li>
 * </ol>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class Processor extends SimulationComponent {

  /**
   * Number of general registers. Fixed to 4.
   */
  private static final int GENERAL_REGISTERS = 4;

  /**
   * The reset value of {@link #ip}. This is set to EPROM_BEG from
   * {@link microsim.simulation.component.MemorySpace}.
   */
  private static final char RESET_INSTRUCTION_ADDRESS = MemorySpace.EPROM_BEG;

  /**
   * Enum representing all possible states of a processor in its instruction cycle. These can be
   * categorized as follows:
   * <ul>
   * <li>
   * Instruction fetch and decode:
   * <ul>
   * <li>{@link #FETCH}.</li>
   * <li>{@link #DECODE}.</li>
   * </ul>
   * </li>
   * <li>
   * Execution states:
   * <ul>
   * <li>Movement instructions:{@link #MOV}, {@link #MOV_IMMEDIATE}, {@link #LOAD},
   * {@link #LOAD_POST}, {@link #LOAD_IMMEDIATE}, {@link #STORE}, {@link #STORE_IMMEDIATE}.</li>
   * <li>Arithmetic instructions: {@link #ADD}, {@link #ADD_IMMEDIATE}, {@link #SUB},
   * {@link #SUB_IMMEDIATE}, {@link #CMP}, {@link #CMP_IMMEDIATE}, {@link #INC}, {@link #DEC}.</li>
   * <li>Logical instructions: {@link #AND}, {@link #AND_IMMEDIATE}, {@link #OR},
   * {@link #OR_IMMEDIATE}, {@link #NOT}.</li>
   * <li>Utility instructions: {@link #SHL}, {@link #SHL_IMMEDIATE}, {@link #SHR},
   * {@link #SHR_IMMEDIATE}.</li>
   * <li>Stack Operations: {@link #PUSH}, {@link #POP}, {@link #POP_POST}, {@link #CALL},
   * {@link #RET}, {@link #RET_POST}.</li>
   * <li>Jump instructions: {@link #JMP_IMMEDIATE}, {@link #JMP}, {@link #JO_IMMEDIATE},
   * {@link #JO}, {@link #JS_IMMEDIATE}, {@link #JS}, {@link #JZ_IMMEDIATE}, {@link #JZ},
   * {@link #JC_IMMEDIATE}, {@link #JC}.</li>
   * <li>Other instructions: {@link #NOP}, {@link #HLT}.</li>
   * </ul>
   * </li>
   * <li>
   * Memory access routines:
   * <ul>
   * <li>Read: {@link #MEM_READ0}, {@link #MEM_READ1}, {@link #MEM_READ2}</li>
   * <li>Write:
   * {@link #MEM_WRITE0}, {@link #MEM_WRITE1}, {@link #MEM_WRITE2}, {@link #MEM_WRITE3}</li>
   * </ul>
   * </li>
   * </ul>
   */
  public static enum ProcessorState {
    // read and decode states
    FETCH,
    DECODE,
    // execution states
    // movement
    MOV,
    MOV_IMMEDIATE,
    LOAD,
    LOAD_POST,
    LOAD_IMMEDIATE,
    STORE,
    STORE_IMMEDIATE,
    // arithmetic
    ADD,
    ADD_IMMEDIATE,
    SUB,
    SUB_IMMEDIATE,
    CMP,
    CMP_IMMEDIATE,
    INC,
    DEC,
    // logic
    AND,
    AND_IMMEDIATE,
    OR,
    OR_IMMEDIATE,
    NOT,
    // utility
    SHL,
    SHL_IMMEDIATE,
    SHR,
    SHR_IMMEDIATE,
    // stack
    PUSH,
    POP,
    POP_POST,
    CALL,
    RET,
    RET_POST,
    // jumps
    JMP_IMMEDIATE,
    JMP,
    JO_IMMEDIATE,
    JO,
    // JNO_IMMEDIATE,
    // JNO,
    JS_IMMEDIATE,
    JS,
    // JNS_IMMEDIATE,
    // JNS,
    JZ_IMMEDIATE,
    JZ,
    // JNZ_IMMEDIATE,
    // JNZ,
    JC_IMMEDIATE,
    JC,
    // JNC_IMMEDIATE,
    // JNC,
    // other
    NOP,
    HLT,
    // memory access routines
    // read routine
    MEM_READ0,
    MEM_READ1,
    MEM_READ2,
    // write routine
    MEM_WRITE0,
    MEM_WRITE1,
    MEM_WRITE2,
    MEM_WRITE3
  }

  /**
   * Maps opcodes (represented by {@link Character}) to execution states for instructions. Some
   * instructions might be carried away in multiple states: in this case maps to the first execution
   * state.
   */
  Map<Character, ProcessorState> opcodeStrings = Map.ofEntries(
    // movement
    Map.entry((char) 0x0000, ProcessorState.MOV),
    Map.entry((char) 0x8000, ProcessorState.MOV_IMMEDIATE),
    Map.entry((char) 0x0040, ProcessorState.LOAD),
    Map.entry((char) 0x8040, ProcessorState.LOAD_IMMEDIATE),
    Map.entry((char) 0x0080, ProcessorState.STORE),
    Map.entry((char) 0x8080, ProcessorState.STORE_IMMEDIATE),
    // arithmetic
    Map.entry((char) 0x4000, ProcessorState.ADD),
    Map.entry((char) 0xc000, ProcessorState.ADD_IMMEDIATE),
    Map.entry((char) 0x4040, ProcessorState.SUB),
    Map.entry((char) 0xc040, ProcessorState.SUB_IMMEDIATE),
    Map.entry((char) 0x4080, ProcessorState.CMP),
    Map.entry((char) 0xc080, ProcessorState.CMP_IMMEDIATE),
    Map.entry((char) 0x40c0, ProcessorState.INC),
    Map.entry((char) 0x4100, ProcessorState.DEC),
    // logic
    Map.entry((char) 0x2000, ProcessorState.AND),
    Map.entry((char) 0xa000, ProcessorState.AND_IMMEDIATE),
    Map.entry((char) 0x2040, ProcessorState.OR),
    Map.entry((char) 0xa040, ProcessorState.OR_IMMEDIATE),
    Map.entry((char) 0x2080, ProcessorState.NOT),
    // utility
    Map.entry((char) 0x6000, ProcessorState.SHL),
    Map.entry((char) 0xe000, ProcessorState.SHL_IMMEDIATE),
    Map.entry((char) 0x6040, ProcessorState.SHR),
    Map.entry((char) 0xe040, ProcessorState.SHR_IMMEDIATE),
    // stack
    Map.entry((char) 0x1000, ProcessorState.PUSH),
    Map.entry((char) 0x1040, ProcessorState.POP),
    Map.entry((char) 0x9080, ProcessorState.CALL),
    Map.entry((char) 0x10c0, ProcessorState.RET),
    // jumps
    Map.entry((char) 0x0800, ProcessorState.JMP),
    Map.entry((char) 0x8800, ProcessorState.JMP_IMMEDIATE),
    Map.entry((char) 0x0840, ProcessorState.JO),
    Map.entry((char) 0x8840, ProcessorState.JO_IMMEDIATE),
    // Map.entry((char)0x0880, ProcessorState.JNO),
    // Map.entry((char)0x8880, ProcessorState.JNO_IMMEDIATE),
    Map.entry((char) 0x08c0, ProcessorState.JS),
    Map.entry((char) 0x88c0, ProcessorState.JS_IMMEDIATE),
    // Map.entry((char)0x0900, ProcessorState.JNS),
    // Map.entry((char)0x8900, ProcessorState.JNS_IMMEDIATE),
    Map.entry((char) 0x0940, ProcessorState.JZ),
    Map.entry((char) 0x8940, ProcessorState.JZ_IMMEDIATE),
    // Map.entry((char)0x0980, ProcessorState.JNZ),
    // Map.entry((char)0x8980, ProcessorState.JNZ_IMMEDIATE),
    Map.entry((char) 0x09c0, ProcessorState.JC),
    Map.entry((char) 0x89c0, ProcessorState.JC_IMMEDIATE),
    // Map.entry((char)0x0a00, ProcessorState.JNC),
    // Map.entry((char)0x8a00, ProcessorState.JNC_IMMEDIATE),
    // other
    Map.entry((char) 0x4800, ProcessorState.HLT),
    Map.entry((char) 0x4840, ProcessorState.NOP)
  );

  /**
   * Reference to the communication bus the component is mounted on.
   */
  private final Bus bus;

  /**
   * Array of general purpose registers, of size {@link #GENERAL_REGISTERS}.
   */
  private final char[] generalRegisters = new char[GENERAL_REGISTERS];

  /**
   * IP (Instruction Pointer) register.
   */
  private char ip;

  /**
   * SP (Stack Pointer) register.
   */
  private char sp;

  /**
   * OF (Overflow Flag)
   */
  private boolean of; // 0

  /**
   * SF (Sign Flag)
   */
  private boolean sf; // 1

  /**
   * ZF (Zero Flag)
   */
  private boolean zf; // 2

  /**
   * CF (Carry Flag)
   */
  private boolean cf; // 3

  /**
   * Source register index for last decoded instruction. Register indices are as follows:
   * <ol>
   * <li>%a.</li>
   * <li>%b.</li>
   * <li>%c.</li>
   * <li>%d.</li>
   * <li>%ip.</li>
   * <li>%sp.</li>
   * </ol>
   */
  private int sourceIndex;

  /**
   * Destination register index for last decoded instruction. Register indices are same as
   * {@link #destIndex}.
   */
  private int destIndex;

  /**
   * Utility register used by memory access routines. Usage is as follows:
   * <ol>
   * <li>Read routine: temp will contain the read data at the end of the operation.</li>
   * <li>Write routine: temp must contain the data to be written before the beginning of the
   * operation.</li>
   * </ol>
   */
  private char temp;

  /**
   * Current state of processor. Initializes to fetch state.
   */
  private ProcessorState state = ProcessorState.FETCH;

  /**
   * Auxiliary address used for minimal call/return functionality in processor microcode. Basically
   * used as an MJR (Multiway Jump Register).
   */
  private ProcessorState returnState;

  /**
   * Register getter used by {@link microsim.ui.DebugShell} objects displaying shells to show
   * register information.
   *
   * @return processor registers in a {@link Character} array
   */
  public char[] getRegisters() {
    char[] registers = new char[GENERAL_REGISTERS + 2];

    System.arraycopy(generalRegisters, 0, registers, 0, GENERAL_REGISTERS);
    registers[GENERAL_REGISTERS] = ip;
    registers[GENERAL_REGISTERS + 1] = sp;

    return registers;
  }

  /**
   * Flag register getter used by {@link microsim.ui.DebugShell} objects displaying shells in the
   * same context as {@link #getRegisters()}.
   *
   * @return flag register as a {@link Boolean} array
   */
  public boolean[] getFlagRegister() {
    // concatenate and return
    boolean[] flags = {of, sf, zf, cf};

    return flags;
  }

  /**
   * State getter used by {@link microsim.ui.DebugShell} objects displaying shells to show state
   * information.
   *
   * @return current processor state
   */
  public ProcessorState getState() {
    return state;
  }

  /**
   * Instantiates processor, taking a reference to the bus it's mounted on. Resets instruction
   * pointer to {@link #RESET_INSTRUCTION_ADDRESS}
   *
   * @param bus bus the component is mounted on
   */
  public Processor(Bus bus) {
    this.bus = bus;

    // processor instantly takes control of all lines but data
    bus.addressLine.drive(this, (char) 0);

    bus.readEnable.drive(this, false);
    bus.writeEnable.drive(this, false);

    bus.targetSpace.drive(this, false);

    // reset instruction pointer
    ip = RESET_INSTRUCTION_ADDRESS;
  }

  /**
   * Steps by implementing a state machine transition based on {@link #state} and
   * {@link #returnState}.
   */
  @Override
  public void step() {
    switch (state) {
      // fetch
      case FETCH: {
        raiseEvent(new FetchEvent(this, ip));

        // read next instruction at ip
        bus.addressLine.drive(this, ip);
        ip += 2;

        // return to decode after read
        returnState = ProcessorState.DECODE;

        // init read
        state = ProcessorState.MEM_READ0;

        break;
      }

      // decode
      case DECODE: {
        char opcode = temp;

        // bit 15 of opcode calls for another read
        boolean hasImmediate = (opcode & 0x8000) != 0;

        // get execution state from opcode
        // might set register indexees for opcode found
        ProcessorState execState = getExecutionState(temp);

        if (hasImmediate) {
          // read immediate word
          bus.addressLine.drive(this, ip);
          ip += 2;

          // return to execution after read
          returnState = execState;

          // read another word
          state = ProcessorState.MEM_READ0;
        } else {
          // proceed to execution
          state = execState;
        }

        raiseEvent(new DecodeEvent(this, opcode, execState, sourceIndex, destIndex, hasImmediate));

        break;
      }

      // movement
      case MOV: {
        // get data in source register
        char data = getRegisterAtIndex(sourceIndex);
        // set destination register to data
        setRegisterAtIndex(destIndex, data);

        state = ProcessorState.FETCH;

        break;
      }
      case MOV_IMMEDIATE: {
        // temp is immediate data
        // set destination register to data
        setRegisterAtIndex(destIndex, temp);

        state = ProcessorState.FETCH;

        break;
      }
      case LOAD: {
        // get address in source register
        char addr = getRegisterAtIndex(sourceIndex);
        // read data at address
        bus.addressLine.drive(this, addr);

        returnState = ProcessorState.LOAD_POST;

        state = ProcessorState.MEM_READ0;

        break;
      }
      case LOAD_POST: {
        // temp is read data
        // set destination register to data
        setRegisterAtIndex(destIndex, temp);

        state = ProcessorState.FETCH;

        break;
      }
      case LOAD_IMMEDIATE: {
        // temp is immediate address
        // read data at address
        bus.addressLine.drive(this, temp);

        returnState = ProcessorState.LOAD_POST;

        state = ProcessorState.MEM_READ0;

        break;
      }
      case STORE: {
        // get address in destination register
        char addr = getRegisterAtIndex(destIndex);
        // write data at address
        bus.addressLine.drive(this, addr);

        // get data in source register
        char data = getRegisterAtIndex(sourceIndex);
        temp = data; // write routine expects data to write in temp

        returnState = ProcessorState.FETCH;

        state = ProcessorState.MEM_WRITE0;

        break;
      }
      case STORE_IMMEDIATE: {
        // temp is immediate address
        char addr = temp;
        // write data at address
        bus.addressLine.drive(this, addr);

        // get data in source index
        char data = getRegisterAtIndex(sourceIndex);
        temp = data; // write routine expects data to write in temp

        returnState = ProcessorState.FETCH;

        state = ProcessorState.MEM_WRITE0;

        break;
      }

      // arithmetic
      case ADD: {
        // get data in source and destination registers
        char dataS = getRegisterAtIndex(sourceIndex);
        char dataD = getRegisterAtIndex(destIndex);

        // get result setting flags
        char res = doAddition(dataS, dataD);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case ADD_IMMEDIATE: {
        // temp is source data
        char dataS = temp;
        // get data in destination register
        char dataD = getRegisterAtIndex(destIndex);

        // get result setting flags
        char res = doAddition(dataS, dataD);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case SUB: {
        // get data in source and destination registers
        char dataS = getRegisterAtIndex(sourceIndex);
        char dataD = getRegisterAtIndex(destIndex);

        // get result setting flags
        char res = doSubtraction(dataS, dataD);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case SUB_IMMEDIATE: {
        // temp is source data
        char dataS = temp;
        // get data in destination register
        char dataD = getRegisterAtIndex(destIndex);

        // get result setting flags
        char res = doSubtraction(dataS, dataD);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case CMP: {
        // get data in source and destination registers
        char dataS = getRegisterAtIndex(sourceIndex);
        char dataD = getRegisterAtIndex(destIndex);

        // just set flags
        doSubtraction(dataS, dataD);

        state = ProcessorState.FETCH;

        break;
      }
      case CMP_IMMEDIATE: {
        // temp is source data
        char dataS = temp;
        // get data in destination register
        char dataD = getRegisterAtIndex(destIndex);

        // just set flags
        doSubtraction(dataS, dataD);

        state = ProcessorState.FETCH;

        break;
      }
      case INC: {
        char data = getRegisterAtIndex(destIndex);
        setRegisterAtIndex(destIndex, ++data);

        state = ProcessorState.FETCH;

        break;
      }
      case DEC: {
        char data = getRegisterAtIndex(destIndex);
        setRegisterAtIndex(destIndex, --data);

        state = ProcessorState.FETCH;

        break;
      }

      // logic
      case AND: {
        // get data in source and destination registers
        char dataS = getRegisterAtIndex(sourceIndex);
        char dataD = getRegisterAtIndex(destIndex);

        // get result
        char res = (char) (dataS & dataD);
        // set flags
        setLogicFlags(res);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case AND_IMMEDIATE: {
        // temp is source data
        char dataS = temp;
        // get data in destination register
        char dataD = getRegisterAtIndex(destIndex);

        // get result
        char res = (char) (dataS & dataD);
        // set flags
        setLogicFlags(res);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case OR: {
        // get data in source and destination registers
        char dataS = getRegisterAtIndex(sourceIndex);
        char dataD = getRegisterAtIndex(destIndex);

        // get result
        char res = (char) (dataS | dataD);
        // set flags
        setLogicFlags(res);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case OR_IMMEDIATE: {
        // temp is source data
        char dataS = temp;
        // get data in destination register
        char dataD = getRegisterAtIndex(destIndex);

        // get result
        char res = (char) (dataS | dataD);
        // set flags
        setLogicFlags(res);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case NOT: {
        // get data in destination register
        char data = getRegisterAtIndex(destIndex);

        // get result
        char res = (char) ~data;
        // set flags
        setLogicFlags(res);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }

      // utility
      case SHL: {
        // get data in source and destination registers
        char dataS = getRegisterAtIndex(sourceIndex);
        char dataD = getRegisterAtIndex(destIndex);

        // get result setting flags
        char res = doShift(dataS, dataD, false);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case SHL_IMMEDIATE: {
        // temp is source data
        char dataS = temp;
        // get data in destination register
        char dataD = getRegisterAtIndex(destIndex);

        // get result setting flags
        char res = doShift(dataS, dataD, false);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case SHR: {
        // get data in source and destination registers
        char dataS = getRegisterAtIndex(sourceIndex);
        char dataD = getRegisterAtIndex(destIndex);

        // get result setting flags
        char res = doShift(dataS, dataD, true);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }
      case SHR_IMMEDIATE: {
        // temp is source data
        char dataS = temp;
        // get data in destination register
        char dataD = getRegisterAtIndex(destIndex);

        // get result setting flags
        char res = doShift(dataS, dataD, true);

        // write back result to destination register
        setRegisterAtIndex(destIndex, res);

        state = ProcessorState.FETCH;

        break;
      }

      // stack
      case PUSH: {
        // get data in destination register
        char data = getRegisterAtIndex(destIndex);

        temp = data; // write routine expects data to write at temp

        // write data at stack pointer
        bus.addressLine.drive(this, sp);
        sp -= 2;

        returnState = ProcessorState.FETCH;

        state = ProcessorState.MEM_WRITE0;

        break;
      }
      case POP: {
        // read from data at stack pointer
        sp += 2;
        bus.addressLine.drive(this, sp);

        returnState = ProcessorState.POP_POST;

        state = ProcessorState.MEM_READ0;

        break;
      }
      case POP_POST: {
        // temp is read data
        // set destination register to data
        setRegisterAtIndex(destIndex, temp);

        state = ProcessorState.FETCH;

        break;
      }
      case CALL: {
        // temp is ip
        char temp_ip = temp;

        temp = ip; // write routine expects data to write at temp

        // now set jump ip
        ip = temp_ip;

        // write data at stack pointer
        bus.addressLine.drive(this, sp);
        sp -= 2;

        returnState = ProcessorState.FETCH;

        state = ProcessorState.MEM_WRITE0;

        break;
      }
      case RET: {
        // read from data at stack pointer
        sp += 2;
        bus.addressLine.drive(this, sp);

        returnState = ProcessorState.RET_POST;

        state = ProcessorState.MEM_READ0;

        break;
      }
      case RET_POST: {
        // temp is read data
        // set ip to data
        ip = temp;

        state = ProcessorState.FETCH;

        break;
      }

      // jumps
      case JMP_IMMEDIATE: {
        // temp is immediate address
        ip = temp;

        state = ProcessorState.FETCH;

        break;
      }
      case JMP: {
        // get address in destination register
        char addr = getRegisterAtIndex(destIndex);
        ip = addr;

        state = ProcessorState.FETCH;

        break;
      }
      case JO_IMMEDIATE: {
        if (of) {
          // temp is immediate address
          ip = temp;
        }

        state = ProcessorState.FETCH;

        break;
      }
      case JO: {
        if (of) {
          // get address in destination register
          char addr = getRegisterAtIndex(destIndex);
          ip = addr;
        }

        state = ProcessorState.FETCH;

        break;
      }
      case JS_IMMEDIATE: {
        if (sf) {
          // temp is immediate address
          ip = temp;
        }

        state = ProcessorState.FETCH;

        break;
      }
      case JS: {
        if (sf) {
          // get address in destination register
          char addr = getRegisterAtIndex(destIndex);
          ip = addr;
        }

        state = ProcessorState.FETCH;

        break;
      }
      case JZ_IMMEDIATE: {
        if (zf) {
          // temp is immediate address
          ip = temp;
        }

        state = ProcessorState.FETCH;

        break;
      }
      case JZ: {
        if (zf) {
          // get address in destination register
          char addr = getRegisterAtIndex(destIndex);
          ip = addr;
        }

        state = ProcessorState.FETCH;

        break;
      }
      case JC_IMMEDIATE: {
        if (cf) {
          // temp is immediate address
          ip = temp;
        }

        state = ProcessorState.FETCH;

        break;
      }
      case JC: {
        if (cf) {
          // get address in destination register
          char addr = getRegisterAtIndex(destIndex);
          ip = addr;
        }

        state = ProcessorState.FETCH;

        break;
      }

      // other
      case NOP: {
        state = ProcessorState.FETCH;

        // System.out.println("A is " + String.format("%04X", generalRegisters[0] & 0xffff));
        // System.out.println("B is " + String.format("%04X", generalRegisters[1] & 0xffff));
        break;
      }
      case HLT: {
        System.out.println("Halting...");
        System.exit(0);
      }

      // memory read routine
      case MEM_READ0: {
        // expect bus address line to be driven by prior state
        raiseEvent(new BusEvent(this, "read_beg", bus.addressLine.read(), null));

        bus.readEnable.drive(this, true);

        state = ProcessorState.MEM_READ1;

        break;
      }
      case MEM_READ1: {
        bus.readEnable.drive(this, false);

        state = ProcessorState.MEM_READ2;

        break;
      }
      case MEM_READ2: {
        temp = bus.dataLine.read();

        raiseEvent(new BusEvent(this, "read_end", bus.addressLine.read(), temp));

        // return
        state = returnState;

        break;
      }

      // memory write routine
      case MEM_WRITE0: {
        // expect bus address line to be driven by prior state
        raiseEvent(new BusEvent(this, "write_beg", bus.addressLine.read(), temp));

        bus.dataLine.drive(this, temp);

        state = ProcessorState.MEM_WRITE1;

        break;
      }
      case MEM_WRITE1: {
        bus.writeEnable.drive(this, true);

        state = ProcessorState.MEM_WRITE2;

        break;
      }
      case MEM_WRITE2: {
        bus.writeEnable.drive(this, false);

        state = ProcessorState.MEM_WRITE3;

        break;
      }
      case MEM_WRITE3: {
        bus.dataLine.release(this);

        raiseEvent(new BusEvent(this, "write_end", bus.addressLine.read(), null));

        // return
        state = returnState;

        break;
      }
      default: {
        throw new RuntimeException("Unknown processor state");
      }
    }
  }

  /**
   * Performs shift operation.
   *
   * @param dataS source operand
   * @param dataD destination operand
   * @param direction direction of shift
   * @return result of the shift
   */
  private char doShift(char dataS, char dataD, boolean direction) {
    int res;

    // direction = false means left
    if (direction) {
      res = dataD >> dataS;

      // set flags
      cf = ((dataD >> (dataS - 1)) & 0x1) != 0;
    } else {
      res = dataD << dataS;

      // set flags
      cf = (res & 0x10000) != 0;
    }

    setLogicFlags((char) res);

    return (char) res;
  }

  /**
   * Sets SF ad ZD based on a result value.
   *
   * @param data result value
   */
  private void setLogicFlags(char data) {
    sf = (data & 0x8000) != 0;
    zf = data == 0;
  }

  /**
   * Performs addition, setting flags.
   *
   * @param dataS source operand
   * @param dataD destination operand
   * @return addition of source and destination
   */
  private char doAddition(char dataS, char dataD) {
    int res = dataD + dataS;

    of = ((~(dataS ^ dataD) & 0x8000) != 0) // operands have same sign
      && (((dataS ^ res) & 0x8000) != 0); // result has different sign than source
    sf = (res & 0x8000) != 0;
    zf = (res & 0xffff) == 0;
    cf = (res & 0x10000) != 0;

    return (char) res;
  }

  /**
   * Performs subtraction, setting flags.
   *
   * @param dataS source operand
   * @param dataD destination operand
   * @return subtraction of source and destination
   */
  private char doSubtraction(char dataS, char dataD) {
    int res = dataD - dataS;

    of = (((dataS ^ dataD) & 0x8000) != 0) // operands have different signs
      && (((dataD ^ res) & 0x8000) != 0); // result has different sign than destination
    sf = (res & 0x8000) != 0;
    zf = (res & 0xffff) == 0;
    cf = dataD < dataS;

    return (char) res;
  }

  /**
   * Gets register at specified index. Register indices are same as {@link #destIndex}.
   *
   * @param index register index
   * @return register value
   */
  private char getRegisterAtIndex(int index) {
    if (index < GENERAL_REGISTERS) {
      return generalRegisters[index];
    }
    // if(index == GENERAL_REGISTERS) {
    // 	return ip;
    // }
    if (index == GENERAL_REGISTERS + 1) {
      return sp;
    }

    throw new RuntimeException("Unknown register");
  }

  /**
   * Sets register at specified index. Register indices are same as {@link #destIndex}.
   *
   * @param index register index
   * @param value value to set register to
   */
  private void setRegisterAtIndex(int index, char value) {
    if (index < GENERAL_REGISTERS) {
      generalRegisters[index] = value;
      return;
    }
    if (index == GENERAL_REGISTERS) {
      ip = value;
      return;
    }
    if (index == GENERAL_REGISTERS + 1) {
      sp = value;
      return;
    }

    throw new RuntimeException("Unknown register");
  }

  /**
   * Gets first execution state from given opcode using {@link #opcodeStrings} map. Also sets
   * {@link #sourceIndex} and {@link #destIndex}. Immediate operand resolution is assumed to be done
   * by caller.
   *
   * @param opcode instruction opcode
   * @return first execution state of instruction
   */
  private ProcessorState getExecutionState(char opcode) {
    // instruction format:
    // opcode[15]: has immediate operand
    // opcode[14:6]: instruction type
    // opcode[5:3]: source operand index
    // opcode[2:0]: destination operand index

    // isolate opcode string
    char opcodeString = (char) (opcode & 0xffc0);

    // get execution state from map
    ProcessorState executionState = opcodeStrings.get(opcodeString);
    if (executionState == null) {
      throw new RuntimeException("Unkown opcode");
    }

    // get register indexes
    destIndex = opcode & 0x07;
    sourceIndex = (opcode >> 3) & 0x07;

    return executionState;
  }
}
