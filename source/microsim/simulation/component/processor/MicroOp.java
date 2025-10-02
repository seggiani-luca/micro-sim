package microsim.simulation.component.processor;

import microsim.simulation.component.Bus;
import static microsim.simulation.component.processor.Decoder.*;

class MicroOp {

  enum OpType {
    DECODE,
    // R format
    ADD_SUB,
    XOR,
    OR,
    AND,
    SLL,
    SRL_SRA,
    SLT,
    SLTU,
    // I format (immediate)
    ADD_I,
    XOR_I,
    OR_I,
    AND_I,
    SLL_I,
    SRL_SRA_I,
    SLT_I,
    SLTU_I,
    // I format (load)
    LOAD_BYTE,
    LOAD_HALF,
    LOAD_WORD,
    LOAD_POST,
    LOAD_POST_U,
    // memory read routine
    MEM_READ0,
    MEM_READ1,
    MEM_READ2,
    // memory write routine
    MEM_WRITE0,
    MEM_WRITE1,
    MEM_WRITE2,
    MEM_WRITE3
  }

  private OpType type;

  private int inst;

  public void setInst(int inst) {
    this.inst = inst;
  }

  public MicroOp(OpType type) {
    this.type = type;
  }

  static int getAddr(Processor proc, int inst) {
    int addr = immI(inst) + proc.getRegister(rs1(inst));
  }

  void execute(Processor proc) {
    switch (type) {
      // decode
      case DECODE -> {
        // temp contains next instruction
        Decoder.decode(proc, proc.temp);
      }

      // R format
      case ADD_SUB -> {
        switch (funct7(inst)) {
          case 0x00 -> // add
            proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) + proc.getRegister(rs2(inst)));

          case 0x20 -> // sub
            proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) - proc.getRegister(rs2(inst)));

          default ->
            throw new RuntimeException("Invalid funct7 value for R Instruction 0x0 (add/sub)");
        }
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
        int shamt = proc.getRegister(rs2(inst)) & 0x1f;
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) << shamt);
      }
      case SRL_SRA -> {
        int shamt = proc.getRegister(rs2(inst)) & 0x1f;

        switch (funct7(inst)) {
          case 0x00 -> // srl
            proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) >>> shamt);

          case 0x20 -> // sra
            proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) >> shamt);

          default ->
            throw new RuntimeException("Invalid funct7 value for R Instruction 0x5 (srl/sra)");
        }
      }
      case SLT -> {
        proc.setRegister(rd(inst), (proc.getRegister(rs1(inst)) < proc.getRegister(rs2(inst))) ? 1 : 0);
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
        int shamt = immI(inst) & 0x1f;
        proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) << shamt);
      }
      case SRL_SRA_I -> {
        int shamt = immI(inst) & 0x1f;

        switch (immI(inst) >>> 5) {
          case 0x00 -> // srl
            proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) >>> shamt);

          case 0x20 -> // sra
            proc.setRegister(rd(inst), proc.getRegister(rs1(inst)) >> shamt);

          default ->
            throw new RuntimeException("Invalid funct7 value for R Instruction 0x5 (srl/sra)");
        }
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
        BusInterface.doReadRoutine(proc, getAddr(proc, inst), Bus.BYTE_SELECT.BYTE);
      }
      case LOAD_HALF -> {
        BusInterface.doReadRoutine(proc, getAddr(proc, inst), Bus.BYTE_SELECT.HALF);
      }
      case LOAD_WORD -> {
        BusInterface.doReadRoutine(proc, getAddr(proc, inst), Bus.BYTE_SELECT.WORD);
      }

      case LOAD_POST -> {
        // temp is read data
        int signed = signExtend(proc.temp, 8);

        proc.setRegister(rd(inst), signed);
      }
      case LOAD_POST_U -> {
        // temp is read data
        proc.setRegister(rd(inst), proc.temp);
      }

      // memory read routine
      case MEM_READ0 -> {
        proc.bus.readEnable.drive(proc, true);
      }
      case MEM_READ1 -> {
        proc.bus.readEnable.drive(proc, false);
      }
      case MEM_READ2 -> {
        proc.temp = proc.bus.dataLine.read();
      }

      // memory write routine
      case MEM_WRITE0 -> {
        proc.bus.dataLine.drive(proc, proc.temp);
      }
      case MEM_WRITE1 -> {
        proc.bus.writeEnable.drive(proc, true);
      }
      case MEM_WRITE2 -> {
        proc.bus.writeEnable.drive(proc, false);
      }
      case MEM_WRITE3 -> {
        proc.bus.dataLine.release(proc);
      }
    }
  }
}
