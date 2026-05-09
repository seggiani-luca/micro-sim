package microsim.simulation.component.processor;

import microsim.simulation.component.bus.*;
import microsim.simulation.component.bus.Bus.ByteSelect;
import static microsim.simulation.component.processor.Decoder.*;
import microsim.simulation.event.*;
import microsim.ui.DebugShell;

/**
 * Implements a micro operation (microop) the
 * {@link microsim.simulation.component.processor.Processor} can execute. Each instruction is split
 * into one or more microops.
 */
public class MicroOp {

  /**
   * Enum of microop types. These are a superset of the implemented ISA.
   */
  public static enum OpType {
    /**
     * Decodes a microop, parses the instruction in temp and appends it to the queue.
     */
    DECODE,
    /**
     * Integer arithmetic addition.
     */
    ADD,
    /**
     * Integer arithmetic subtraction.
     */
    SUB,
    /**
     * Boolean arithmetic XOR.
     */
    XOR,
    /**
     * Boolean arithmetic OR.
     */
    OR,
    /**
     * Boolean arithmetic AND.
     */
    AND,
    /**
     * Boolean arithmetic logical shift left.
     */
    SLL,
    /**
     * Boolean arithmetic logical shift right.
     */
    SRL,
    /**
     * Boolean arithmetic arithmetic shift right.
     */
    SRA,
    /**
     * Set if less.
     */
    SLT,
    /**
     * Set if less unsigned.
     */
    SLTU,
    /**
     * Integer arithmetic immediate addition.
     */
    ADD_I,
    /**
     * Boolean arithmetic immediate XOR.
     */
    XOR_I,
    /**
     * Boolean arithmetic immediate OR.
     */
    OR_I,
    /**
     * Boolean arithmetic immediate AND.
     */
    AND_I,
    /**
     * Boolean arithmetic immediate logical shift left.
     */
    SLL_I,
    /**
     * Boolean arithmetic immediate logical shift right.
     */
    SRL_I,
    /**
     * Boolean arithmetic immediate arithmetic shift left.
     */
    SRA_I,
    /**
     * Set if less than immediate.
     */
    SLT_I,
    /**
     * Set if less than immediate unsigned.
     */
    SLTU_I,
    /**
     * Loads a byte into temp.
     */
    LOAD_BYTE,
    /**
     * Loads an half into temp.
     */
    LOAD_HALF,
    /**
     * Loads a word into temp.
     */
    LOAD_WORD,
    /**
     * Loads data from temp to target register.
     */
    LOAD_POST,
    /**
     * Loads unsigned data from temp to target register.
     */
    LOAD_POST_U,
    /**
     * Stores a byte to memory.
     */
    STORE_BYTE,
    /**
     * Stores an half to memory.
     */
    STORE_HALF,
    /**
     * Stores a word to memory.
     */
    STORE_WORD,
    /**
     * Branch on equal.
     */
    BRANCH_EQ,
    /**
     * Branch on not equal.
     */
    BRANCH_NE,
    /**
     * Branch on less than.
     */
    BRANCH_LT,
    /**
     * Branch on greater or equal.
     */
    BRANCH_GE,
    /**
     * Branch on less than unsigned.
     */
    BRANCH_LTU,
    /**
     * Branch on greater or equal unsigned.
     */
    BRANCH_GEU,
    /**
     * Jump and link.
     */
    JAL,
    /**
     * Jump and link with register.
     */
    JAL_REG,
    /**
     * Load upper immediate.
     */
    LUI,
    /**
     * Add upper immediate to program counter.
     */
    AUIPC,
    /**
     * Environment call (call or break).
     */
    ENV,
    /**
     * Steps execution (increases program counter).
     */
    EXEC_POST,
    /**
     * Step 1 of read routine
     */
    MEM_READ1,
    /**
     * Step 2 of read routine
     */
    MEM_READ2,
    /**
     * Step 1 of write routine
     */
    MEM_WRITE1,
  }

  /**
   * Type of microop.
   */
  private final OpType type;

  /**
   * Returns type of microop.
   *
   * @return type of microop
   */
  public OpType getType() {
    return type;
  }

  /**
   * Instruction this microop translates. Is used to fetch instruction-specific fields at execution
   * time.
   */
  private final int inst;

  /**
   * Returns instruction microop translates.
   *
   * @return instruction this microop translates
   */
  public int getInstruction() {
    return inst;
  }

  /**
   * Constructs a microop from its type and the associated instruction.
   *
   * @param type type of microop
   * @param inst instruction microop encodes
   */
  public MicroOp(OpType type, int inst) {
    this.type = type;
    this.inst = inst;
  }

  /**
   * Constructs a microop from its type.
   *
   * @param type type of microop
   */
  public MicroOp(OpType type) {
    this.type = type;
    this.inst = 0;
  }

  /**
   * Gets the address for load instructions.
   *
   * @param proc processor instance to run on
   * @param inst load instruction
   * @return address to load from
   */
  static int getAddrL(Processor proc, int inst) {
    return immI(inst) + proc.getRegister(rs1(inst));
  }

  /**
   * Gets the address for store instructions.
   *
   * @param proc processor instance to run on
   * @param inst load instruction
   * @return address to load from
   */
  static int getAddrS(Processor proc, int inst) {
    return immS(inst) + proc.getRegister(rs1(inst));
  }

  /**
   * Get shift amount for shift instructions.
   *
   * @param proc processor instance to run on
   * @param inst load instruction
   * @return amount to shift by
   */
  static int getShamt(Processor proc, int inst) {
    return proc.getRegister(rs2(inst)) & 0x1f;
  }

  /**
   * Get shift amount for immediate shift instructions. Processor instance not needed as its fully
   * immediate.
   *
   * @param inst load instruction
   * @return amount to shift by
   */
  static int getShamtImmediate(int inst) {
    return immI(inst) & 0x1f;
  }

  /**
   * Executes a microop on a processor instance.
   *
   * @param proc processor instance to run on
   */
  void execute(Processor proc) {
    switch (type) {
      // decode
      case DECODE -> {
        // temp contains next instruction
        Decoder.decode(proc, proc.temp);
      }

      // R format
      case ADD -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) + proc.getRegister(rs2(inst)));
      }
      case SUB -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) - proc.getRegister(rs2(inst)));
      }
      case XOR -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) ^ proc.getRegister(rs2(inst)));
      }
      case OR -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) | proc.getRegister(rs2(inst)));
      }
      case AND -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) & proc.getRegister(rs2(inst)));
      }
      case SLL -> {
        int shamt = getShamt(proc, inst);
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) << shamt);
      }
      case SRL -> {
        int shamt = getShamt(proc, inst);
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) >>> shamt);

      }
      case SRA -> {
        int shamt = getShamt(proc, inst);
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) >> shamt);
      }
      case SLT -> {
        proc.setRegister(rd(inst),
                (proc.getRegister(rs1(inst)) < proc.getRegister(rs2(inst))) ? 1 : 0
        );
      }
      case SLTU -> {
        int op1 = proc.getRegister(rs1(inst));
        int op2 = proc.getRegister(rs2(inst));

        proc.setRegister(rd(inst), (Integer.compareUnsigned(op1, op2) < 0) ? 1 : 0);
      }

      // I format (immediate)
      case ADD_I -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) + immI(inst));
      }
      case XOR_I -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) ^ immI(inst));
      }
      case OR_I -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) | immI(inst));
      }
      case AND_I -> {
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) & immI(inst));
      }
      case SLL_I -> {
        int shamt = getShamtImmediate(inst);
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) << shamt);
      }
      case SRL_I -> {
        int shamt = getShamtImmediate(inst);
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) >>> shamt);
      }
      case SRA_I -> {
        int shamt = getShamtImmediate(inst);
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) >> shamt);
      }
      case SLT_I -> {
        proc.setRegister(rd(inst), (proc.getRegister(rs1(inst)) < immI(inst)) ? 1 : 0);
      }
      case SLTU_I -> {
        int op1 = proc.getRegister(rs1(inst));
        int op2 = immI(inst);

        proc.setRegister(rd(inst), (Integer.compareUnsigned(op1, op2) < 0) ? 1 : 0);
      }

      // I format (load)
      case LOAD_BYTE -> {
        BusInterface.doReadRoutine(proc, getAddrL(proc, inst), Bus.ByteSelect.BYTE);
      }
      case LOAD_HALF -> {
        BusInterface.doReadRoutine(proc, getAddrL(proc, inst), Bus.ByteSelect.HALF);
      }
      case LOAD_WORD -> {
        BusInterface.doReadRoutine(proc, getAddrL(proc, inst), Bus.ByteSelect.WORD);
      }

      case LOAD_POST -> {
        // temp is read data
        int size = 0;
        switch (proc.byteSelect) {
          case ByteSelect.BYTE ->
            size = 8;
          case ByteSelect.HALF ->
            size = 16;
          case ByteSelect.WORD ->
            size = 32;
        }

        int signed = signExtend(proc.temp, size);

        proc.setRegister(rd(inst), signed);
      }
      case LOAD_POST_U -> {
        // temp is read data
        proc.setRegister(rd(inst), proc.temp);
      }

      // S format
      case STORE_BYTE -> {
        BusInterface.doWriteRoutine(proc, getAddrS(proc, inst), proc.getRegister(rs2(inst)),
                Bus.ByteSelect.BYTE);
      }
      case STORE_HALF -> {
        BusInterface.doWriteRoutine(proc, getAddrS(proc, inst), proc.getRegister(rs2(inst)),
                Bus.ByteSelect.HALF);
      }
      case STORE_WORD -> {
        BusInterface.doWriteRoutine(proc, getAddrS(proc, inst), proc.getRegister(rs2(inst)),
                Bus.ByteSelect.WORD);
      }

      // B format
      case BRANCH_EQ -> {
        if (proc.getRegister(rs1(inst)) == proc.getRegister(rs2(inst))) {
          proc.pc += immB(inst);
        } else {
          proc.pc += 4;
        }
      }
      case BRANCH_NE -> {
        if (proc.getRegister(rs1(inst)) != proc.getRegister(rs2(inst))) {
          proc.pc += immB(inst);
        } else {
          proc.pc += 4;
        }
      }
      case BRANCH_LT -> {
        if (proc.getRegister(rs1(inst)) < proc.getRegister(rs2(inst))) {
          proc.pc += immB(inst);
        } else {
          proc.pc += 4;
        }
      }
      case BRANCH_GE -> {
        if (proc.getRegister(rs1(inst)) >= proc.getRegister(rs2(inst))) {
          proc.pc += immB(inst);
        } else {
          proc.pc += 4;
        }
      }
      case BRANCH_LTU -> {
        if (Integer.compareUnsigned(proc.getRegister(rs1(inst)),
                proc.getRegister(rs2(inst))) < 0) {
          proc.pc += immB(inst);
        } else {
          proc.pc += 4;
        }
      }
      case BRANCH_GEU -> {
        if (Integer.compareUnsigned(proc.getRegister(rs1(inst)),
                proc.getRegister(rs2(inst))) >= 0) {
          proc.pc += immB(inst);
        } else {
          proc.pc += 4;
        }
      }

      // J format
      case JAL -> {
        proc.setRegister(rd(inst), proc.pc + 4);
        proc.pc += immJ(inst);
      }
      // I format (jump)
      case JAL_REG -> {
        proc.setRegister(rd(inst), proc.pc + 4);
        proc.pc = proc.getRegister(rs1(inst)) + immI(inst);
      }

      // U format
      case LUI -> {
        proc.setRegister(rd(inst), immU(inst));
      }
      case AUIPC -> {
        proc.setRegister(rd(inst), proc.pc + immU(inst));
      }

      // I format (environment)
      case ENV -> {
        switch (immI(inst)) {
          case 0x00 -> { // ecall
            proc.raiseEvent(new HaltEvent(proc));
          }

          case 0x01 -> { // ebreak
            proc.raiseEvent(new BreakEvent(proc));
          }

          default ->
            throw new RuntimeException("Invalid immediate for environment call");
        }
      }

      // post execution
      case EXEC_POST -> {
        proc.pc += 4;
      }

      // memory read routine (step 0 is done by bus interface)
      case MEM_READ1 -> {
        // lower control line
        proc.bus.readEnable.driveBool(proc, false);
      }
      case MEM_READ2 -> {
        // read data from bus
        proc.temp = proc.bus.dataLine.read();

        // log read data
        proc.raiseEvent(new DebugEvent(proc, "Processor read routine finished and got value "
                + DebugShell.int32ToString(proc.temp)));
      }

      // memory write routine (step 0 is done by bus interface)
      case MEM_WRITE1 -> {
        // lower control line
        proc.bus.writeEnable.driveBool(proc, false);

        // release data line
        proc.bus.dataLine.release(proc);

        // log data routine finished
        proc.raiseEvent(new DebugEvent(proc, "Processor write routine finished"));
      }
    }
  }

  @Override
  public String toString() {
    return type.name() + " - (" + (inst == 0 ? "freestanding" : DebugShell.int32ToString(inst))
            + ")";
  }
}
