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
    DECODE,
    // R format
    ADD,
    SUB,
    XOR,
    OR,
    AND,
    SLL,
    SRL,
    SRA,
    SLT,
    SLTU,
    // I format (immediate)
    ADD_I,
    XOR_I,
    OR_I,
    AND_I,
    SLL_I,
    SRL_I,
    SRA_I,
    SLT_I,
    SLTU_I,
    // I format (load)
    LOAD_BYTE,
    LOAD_HALF,
    LOAD_WORD,
    LOAD_POST,
    LOAD_POST_U,
    // S format
    STORE_BYTE,
    STORE_HALF,
    STORE_WORD,
    // B format
    BRANCH_EQ,
    BRANCH_NE,
    BRANCH_LT,
    BRANCH_GE,
    BRANCH_LTU,
    BRANCH_GEU,
    // I format (jump)
    JAL,
    JAL_REG,
    // U format
    LUI,
    AUIPC,
    // I format (environment)
    ENV,
    // post execution
    EXEC_POST,
    // memory read routine
    MEM_READ0,
    MEM_READ1,
    // memory write routine
    MEM_WRITE0,
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
            System.out.println("Environment call, halting...");
            System.exit(0);
          }

          case 0x01 -> { // ebreak
            System.out.println("Environment break, launching debugger...");
            proc.raiseEvent(new AttachEvent(proc));
          }

          default ->
            throw new RuntimeException("Invalid immediate for environment call");
        }
      }

      // post execution
      case EXEC_POST -> {
        proc.pc += 4;
      }

      // memory read routine
      case MEM_READ0 -> {
        proc.bus.readEnable.drive(proc, 0);
      }
      case MEM_READ1 -> {
        proc.temp = proc.bus.dataLine.read();

        if (DebugShell.active) {
          proc.raiseEvent(new DebugEvent(proc, "Processor read routine finished and got value "
            + DebugShell.int32ToString(proc.temp)));
        }
      }

      // memory write routine
      case MEM_WRITE0 -> {
        proc.bus.writeEnable.drive(proc, 0);
        proc.bus.dataLine.release(proc);

        if (DebugShell.active) {
          proc.raiseEvent(new DebugEvent(proc, "Processor write routine finished"));
        }
      }
    }
  }

  @Override
  public String toString() {
    return type.name() + " - (" + (inst == 0 ? "freestanding" : DebugShell.int32ToString(inst))
      + ")";
  }
}
