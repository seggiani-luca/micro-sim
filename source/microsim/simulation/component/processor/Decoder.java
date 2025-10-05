package microsim.simulation.component.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import microsim.simulation.component.processor.MicroOp.OpType;

/**
 * Decodes instruction into microop sequences through a static {@link #decode()} method.
 */
public class Decoder {

  /**
   * Sign extends to 32 bits an integer smaller than 32 bits.
   *
   * @param val integer to extend
   * @param bits bits the integer is defined on
   * @return integer, sign extended to 32 bits
   */
  static int signExtend(int val, int bits) {
    int shift = 32 - bits;
    return (val << shift) >> shift;
  }

  /**
   * Extracts opcode from an rv32i instruction.
   *
   * @param inst instruction
   * @return opcode field
   */
  static int opcode(int inst) {
    return inst & 0x7f;
  }

  /**
   * Extracts funct3 from an rv32i instruction.
   *
   * @param inst instruction
   * @return funct3 field
   */
  static int funct3(int inst) {
    return (inst >>> 12) & 0x7;
  }

  /**
   * Extracts funct7 from an rv32i instruction.
   *
   * @param inst instruction
   * @return funct7 field
   */
  static int funct7(int inst) {
    return (inst >>> 25) & 0x7f;
  }

  /**
   * Extracts first source register index from an rv32i instruction.
   *
   * @param inst instruction
   * @return rs1 field
   */
  static int rs1(int inst) {
    return (inst >>> 15) & 0x1f;
  }

  /**
   * Extracts second source register from an rv32i instruction.
   *
   * @param inst instruction
   * @return rs2 field
   */
  static int rs2(int inst) {
    return (inst >>> 20) & 0x1f;
  }

  /**
   * Extracts target register from an rv32i instruction.
   *
   * @param inst instruction
   * @return rd field
   */
  static int rd(int inst) {
    return (inst >>> 7) & 0x1f;
  }

  /**
   * Extracts I format immediate from an rv32i instruction.
   *
   * @param inst instruction
   * @return immediate field
   */
  static int immI(int inst) {
    int i = (inst >>> 20) & 0xfff;
    return signExtend(i, 12);
  }

  /**
   * Extracts S format immediate from an rv32i instruction.
   *
   * @param inst instruction
   * @return immediate field
   */
  static int immS(int inst) {
    int i11_5 = (inst >>> 25) & 0x7f;
    int i4_0 = (inst >>> 7) & 0x1f;

    int i = (i11_5 << 5) | i4_0;
    return signExtend(i, 12);
  }

  /**
   * Extracts B format immediate from an rv32i instruction.
   *
   * @param inst instruction
   * @return immediate field
   */
  static int immB(int inst) {
    int i12 = (inst >>> 31) & 0x1;
    int i10_5 = (inst >>> 25) & 0x3f;
    int i4_1 = (inst >>> 8) & 0xf;
    int i11 = (inst >>> 7) & 0x1;

    int i = (i12 << 12) | (i11 << 11) | (i10_5 << 5) | (i4_1 << 1);
    return signExtend(i, 13);
  }

  /**
   * Extracts U format immediate from an rv32i instruction.
   *
   * @param inst instruction
   * @return immediate field
   */
  static int immU(int inst) {
    int i = (inst >>> 12) & 0xfffff;
    return i << 12;
  }

  /**
   * Extracts J format immediate from an rv32i instruction.
   *
   * @param inst instruction
   * @return immediate field
   */
  static int immJ(int inst) {
    int i20 = (inst >>> 31) & 0x1;
    int i10_1 = (inst >>> 21) & 0x3ff;
    int i11 = (inst >>> 20) & 0x1;
    int i19_12 = (inst >>> 12) & 0xff;

    int i = (i20 << 20) | (i19_12 << 12) | (i11 << 11) | (i10_1 << 1);
    return signExtend(i, 21);
  }

  /**
   * Opcode for R format instructions.
   */
  static final int R_OPCODE = 0x33;

  /**
   * Opcode for I format (immediate) instructions.
   */
  static final int II_OPCODE = 0x13;

  /**
   * Opcode for I format (load) instructions.
   */
  static final int IL_OPCODE = 0x03;

  /**
   * Opcode for S format instructions.
   */
  static final int S_OPCODE = 0x23;

  /**
   * Opcode for B format instructions.
   */
  static final int B_OPCODE = 0x63;

  /**
   * Opcode for J format instructions.
   */
  static final int J_OPCODE = 0x6f;
  /**
   * Opcode for I format (jump) instructions.
   */
  static final int IJ_OPCODE = 0x67;

  /**
   * Opcode for U format (load) instructions.
   */
  static final int UL_OPCODE = 0x37;
  /**
   * Opcode for U format (add) instructions.
   */
  static final int UA_OPCODE = 0x17;

  /**
   * Opcode for I format (environment) instructions.
   */
  static final int IE_OPCODE = 0x73;

  /**
   * Gets funct3 to select from secondary map. For instructions where funct3 is not significant,
   * returns 0.
   *
   * @param inst instruction to get funct3
   * @return funct3 to index secondary map
   */
  static int getFunct3(int inst) {
    int opcode = opcode(inst);
    switch (opcode) {
      case R_OPCODE, II_OPCODE, IL_OPCODE, S_OPCODE, B_OPCODE, IJ_OPCODE, IE_OPCODE -> {
        // these either distinguish or enforce funct3 to 0x00
        return funct3(inst);
      }
      case J_OPCODE, UL_OPCODE, UA_OPCODE -> {
        // these don't have funct3
        return 0;
      }
      default -> {
        throw new RuntimeException("Invalid opcode");
      }
    }
  }

  /**
   * Dual map to index instructions. TODO: a trie would be better than a dual map setup.
   */
  static final Map<Integer, Map<Integer, List<MicroOp>>> instMap = new HashMap();

  // setup dual map
  static {
    // R format
    Map<Integer, List<MicroOp>> rInstMap = new HashMap();

    // fill R format
    rInstMap.put(0x0, List.of(
      new MicroOp(OpType.ADD_SUB)
    ));
    rInstMap.put(0x4, List.of(
      new MicroOp(OpType.XOR)
    ));
    rInstMap.put(0x6, List.of(
      new MicroOp(OpType.OR)
    ));
    rInstMap.put(0x7, List.of(
      new MicroOp(OpType.AND)
    ));
    rInstMap.put(0x1, List.of(
      new MicroOp(OpType.SLL)
    ));
    rInstMap.put(0x5, List.of(
      new MicroOp(OpType.SRL_SRA)
    ));
    rInstMap.put(0x2, List.of(
      new MicroOp(OpType.SLT)
    ));
    rInstMap.put(0x3, List.of(
      new MicroOp(OpType.SLTU)
    ));

    // insert R format
    instMap.put(R_OPCODE, rInstMap);

    // I format (immediate)
    Map<Integer, List<MicroOp>> iiInstMap = new HashMap();

    // fill I format (immediate)
    iiInstMap.put(0x0, List.of(
      new MicroOp(OpType.ADD_I)
    ));
    iiInstMap.put(0x4, List.of(
      new MicroOp(OpType.XOR_I)
    ));
    iiInstMap.put(0x6, List.of(
      new MicroOp(OpType.OR_I)
    ));
    iiInstMap.put(0x7, List.of(
      new MicroOp(OpType.AND_I)
    ));
    iiInstMap.put(0x1, List.of(
      new MicroOp(OpType.SLL_I)
    ));
    iiInstMap.put(0x5, List.of(
      new MicroOp(OpType.SRL_SRA_I)
    ));
    iiInstMap.put(0x2, List.of(
      new MicroOp(OpType.SLT_I)
    ));
    iiInstMap.put(0x3, List.of(
      new MicroOp(OpType.SLTU_I)
    ));

    // insert I format (immediate)
    instMap.put(II_OPCODE, iiInstMap);

    // I format (load)
    Map<Integer, List<MicroOp>> ilInstMap = new HashMap();

    // fill I format (load)
    ilInstMap.put(0x0, List.of(
      new MicroOp(OpType.LOAD_BYTE),
      new MicroOp(OpType.LOAD_POST)
    ));
    ilInstMap.put(0x1, List.of(
      new MicroOp(OpType.LOAD_HALF),
      new MicroOp(OpType.LOAD_POST)
    ));
    ilInstMap.put(0x2, List.of(
      new MicroOp(OpType.LOAD_WORD),
      new MicroOp(OpType.LOAD_POST)
    ));
    ilInstMap.put(0x4, List.of(
      new MicroOp(OpType.LOAD_BYTE),
      new MicroOp(OpType.LOAD_POST_U)
    ));
    ilInstMap.put(0x5, List.of(
      new MicroOp(OpType.LOAD_HALF),
      new MicroOp(OpType.LOAD_POST_U)
    ));

    // insert I format (load)
    instMap.put(IL_OPCODE, ilInstMap);

    // S format
    Map<Integer, List<MicroOp>> sInstMap = new HashMap();

    // fill S format
    sInstMap.put(0x0, List.of(
      new MicroOp(OpType.STORE_BYTE)
    ));
    sInstMap.put(0x1, List.of(
      new MicroOp(OpType.STORE_HALF)
    ));
    sInstMap.put(0x2, List.of(
      new MicroOp(OpType.STORE_WORD)
    ));

    // insert S format
    instMap.put(S_OPCODE, sInstMap);

    // B format
    Map<Integer, List<MicroOp>> bInstMap = new HashMap();

    // fill B format
    bInstMap.put(0x0, List.of(
      new MicroOp(OpType.BRANCH_EQ)
    ));
    bInstMap.put(0x1, List.of(
      new MicroOp(OpType.BRANCH_NE)
    ));
    bInstMap.put(0x4, List.of(
      new MicroOp(OpType.BRANCH_LT)
    ));
    bInstMap.put(0x5, List.of(
      new MicroOp(OpType.BRANCH_GE)
    ));
    bInstMap.put(0x6, List.of(
      new MicroOp(OpType.BRANCH_LTU)
    ));
    bInstMap.put(0x7, List.of(
      new MicroOp(OpType.BRANCH_GEU)
    ));

    // insert B format
    instMap.put(B_OPCODE, bInstMap);

    // J format
    Map<Integer, List<MicroOp>> jInstMap = new HashMap();

    // fill J format
    jInstMap.put(0, List.of(
      new MicroOp(OpType.JAL)
    ));

    // insert J format
    instMap.put(J_OPCODE, jInstMap);

    // I format (jump)
    Map<Integer, List<MicroOp>> ijInstMap = new HashMap();

    // fill I format (jump)
    ijInstMap.put(0x0, List.of(
      new MicroOp(OpType.JAL_REG)
    ));

    // insert I format (jump)
    instMap.put(IJ_OPCODE, ijInstMap);

    // U format (load, add)
    Map<Integer, List<MicroOp>> ulInstMap = new HashMap();
    Map<Integer, List<MicroOp>> uaInstMap = new HashMap();

    // fill U format (load, add)
    ulInstMap.put(0, List.of(
      new MicroOp(OpType.LUI)
    ));
    uaInstMap.put(0, List.of(
      new MicroOp(OpType.AUIPC)
    ));

    // insert U format (load, add)
    instMap.put(UL_OPCODE, ulInstMap);
    instMap.put(UA_OPCODE, uaInstMap);

    // I format (environment)
    Map<Integer, List<MicroOp>> ieFormat = new HashMap();

    // fill I format
    ieFormat.put(0x0, List.of(
      new MicroOp(OpType.ENV)
    ));

    instMap.put(IE_OPCODE, ieFormat);
  }

  /**
   * Decodes an instruction into a sequence of microops, and queues them to a processor instance.
   *
   * @param proc processor instance
   * @param inst instruction to decode
   */
  public static void decode(Processor proc, int inst) {
    // get opcode, funct3 pair to query maps
    int opcode = opcode(inst);
    int funct3 = getFunct3(inst);

    // get microop
    List<MicroOp> opList = instMap.get(opcode).get(funct3);

    // iterate completing microops and pushing to processor queue
    for (MicroOp op : opList) {
      op.setInst(inst);
      proc.opQueue.add(op);
    }
  }
}
