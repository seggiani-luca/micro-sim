package microsim.simulation.component.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import microsim.simulation.component.processor.MicroOp.OpType;

public class Decoder {

  static int signExtend(int val, int bits) {
    int shift = 32 - bits;
    return (val << shift) >> shift;
  }

  static int funct3(int inst) {
    return (inst >>> 12) & 0x7;
  }

  static int funct7(int inst) {
    return (inst >>> 25) & 0x7f;
  }

  static int rs1(int inst) {
    return (inst >>> 15) & 0x1f;
  }

  static int rs2(int inst) {
    return (inst >>> 20) & 0x1f;
  }

  static int rd(int inst) {
    return (inst >>> 7) & 0x1f;
  }

  static int immI(int inst) {
    int i = (inst >>> 20) & 0xfff;
    return signExtend(i, 12);
  }

  static int immS(int inst) {
    int i11_5 = (inst >>> 25) & 0x7f;
    int i4_0 = (inst >>> 7) & 0x1f;

    int i = (i11_5 << 5) | i4_0;
    return signExtend(i, 12);
  }

  static int immB(int inst) {
    int i12 = (inst >>> 31) & 0x1;
    int i10_5 = (inst >>> 20) & 0x3f;
    int i4_1 = (inst >>> 8) & 0xf;
    int i11 = (inst >>> 7) & 0x1;

    int i = (i12 << 12) | (i11 << 11) | (i10_5 << 5) | (i4_1 << 1);
    return signExtend(i, 13);
  }

  static int immU(int inst) {
    int i = (inst >>> 12) & 0xfffff;
    return i << 20;
  }

  static int immJ(int inst) {
    int i20 = (inst >>> 31) & 0x1;
    int i10_1 = (inst >>> 21) & 0x3ff;
    int i11 = (inst >>> 20) & 0x1;
    int i19_12 = (inst >>> 12) & 0xff;

    int i = (i20 << 20) | (i19_12 << 12) | (i11 << 11) | (i10_1 << 1);
    return signExtend(i, 21);
  }

  static int getFunct(int inst) {
    // TODO: should return 0 on instructions that dont use the nested map
  }

  static final Map<Integer, Map<Integer, List<MicroOp>>> instMap = new HashMap();

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
    instMap.put(0x33, rInstMap);

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
    instMap.put(0x13, rInstMap);

    // I format (load)
    Map<Integer, List<MicroOp>> ilInstMap = new HashMap();

    // fill I format (load)
    iiInstMap.put(0x0, List.of(
      new MicroOp(OpType.LOAD_BYTE),
      new MicroOp(OpType.LOAD_POST)
    ));
    iiInstMap.put(0x1, List.of(
      new MicroOp(OpType.LOAD_HALF),
      new MicroOp(OpType.LOAD_POST)
    ));
    iiInstMap.put(0x2, List.of(
      new MicroOp(OpType.LOAD_WORD),
      new MicroOp(OpType.LOAD_POST)
    ));
    iiInstMap.put(0x4, List.of(
      new MicroOp(OpType.LOAD_BYTE),
      new MicroOp(OpType.LOAD_POST_U)
    ));
    iiInstMap.put(0x5, List.of(
      new MicroOp(OpType.LOAD_HALF),
      new MicroOp(OpType.LOAD_POST_U)
    ));

    // insert I format (load)
    instMap.put(0x03, ilInstMap);
  }

  public static void decode(Processor proc, int inst) {
    // TODO: implement this using nested maps
  }
}
