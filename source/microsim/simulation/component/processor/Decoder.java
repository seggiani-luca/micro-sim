package microsim.simulation.component.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import microsim.simulation.component.processor.MicroOp.OpType;
import microsim.ui.DebugShell;

/**
 * Defines a trie data structure used to query microop sequences from instruction encodings.
 * Supports putting objects and retrieving them. TODO: should retrieve extracting fields from
 * a single int
 */
class Trie<T> {

  /**
   * Trie node. Holds a hash map to children and a (possibly null) data field.
   *
   * @param <T> trie data type
   */
  class TrieNode<T> {

    /**
     * Map of children trie nodes.
     */
    private final Map<Integer, TrieNode<T>> children = new HashMap<>();

    /**
     * Data held by node. Non leaf trie nodes are signaled by this field being null. Leafs can still
     * have children.
     */
    private T data;
  }

  /**
   * Root of trie.
   */
  private final TrieNode<T> root = new TrieNode<>();

  /**
   * Put an item in the trie at the given key.
   *
   * @param keys key to put item at
   * @param data item to put
   */
  public void put(List<Integer> keys, T data) {
    // traverse trie to data
    TrieNode<T> child = root;
    for (Integer key : keys) {
      child.children.putIfAbsent(key, new TrieNode<>());
      child = child.children.get(key);
    }

    // put data
    child.data = data;
  }

  /**
   * Get an item from the trie at the given key. Returns null if no such item is found. Shorcircuits
   * at the first valid data node if key is longer than available path (keys are expected to be).
   *
   * @param keys key to search item at
   * @return item, if found
   */
  public T get(List<Integer> keys) {
    // traverse trie to data
    TrieNode<T> child = root;
    for (Integer key : keys) {
      // shortcircuit
      if (child.children.get(key) == null) {
        break;
      }

      // traverse normally
      child = child.children.get(key);
    }

    // get data
    return child.data;
  }
}

/**
 * Decodes rv32i instructions into microop sequences through a static
 * {@link #decode(microsim.simulation.component.processor.Processor, int)} method, using the
 * {@link microsim.simulation.component.processor.Trie} class to index.
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
   * Trie from instruction encoding to microop list.
   */
  static final Trie<List<OpType>> instTrie = new Trie<>();

  // setup trie
  static {
    // R format
    instTrie.put(List.of(R_OPCODE, 0x0, 0x00), List.of(
      OpType.ADD,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x0, 0x20), List.of(
      OpType.SUB,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x4, 0x00), List.of(
      OpType.XOR,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x6, 0x00), List.of(
      OpType.OR,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x7, 0x00), List.of(
      OpType.AND,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x1, 0x00), List.of(
      OpType.SLL,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x5, 0x00), List.of(
      OpType.SRL,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x5, 0x20), List.of(
      OpType.SRA,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x2, 0x00), List.of(
      OpType.SLT,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(R_OPCODE, 0x3, 0x00), List.of(
      OpType.SLTU,
      OpType.EXEC_POST
    ));

    // I format (immediate)
    instTrie.put(List.of(II_OPCODE, 0x0), List.of(
      OpType.ADD_I,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(II_OPCODE, 0x4), List.of(
      OpType.XOR_I,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(II_OPCODE, 0x6), List.of(
      OpType.OR_I,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(II_OPCODE, 0x7), List.of(
      OpType.AND_I,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(II_OPCODE, 0x1, 0x00), List.of(
      OpType.SLL_I,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(II_OPCODE, 0x5, 0x00), List.of(
      OpType.SRL_I,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(II_OPCODE, 0x5, 0x20), List.of(
      OpType.SRA_I,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(II_OPCODE, 0x2), List.of(
      OpType.SLT_I,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(II_OPCODE, 0x3), List.of(
      OpType.SLTU_I,
      OpType.EXEC_POST
    ));

    // I format (load)
    instTrie.put(List.of(IL_OPCODE, 0x0), List.of(
      OpType.LOAD_BYTE,
      OpType.LOAD_POST,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(IL_OPCODE, 0x1), List.of(
      OpType.LOAD_HALF,
      OpType.LOAD_POST,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(IL_OPCODE, 0x2), List.of(
      OpType.LOAD_WORD,
      OpType.LOAD_POST,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(IL_OPCODE, 0x4), List.of(
      OpType.LOAD_BYTE,
      OpType.LOAD_POST_U,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(IL_OPCODE, 0x5), List.of(
      OpType.LOAD_HALF,
      OpType.LOAD_POST_U,
      OpType.EXEC_POST
    ));

    // S format
    instTrie.put(List.of(S_OPCODE, 0x0), List.of(
      OpType.STORE_BYTE,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(S_OPCODE, 0x1), List.of(
      OpType.STORE_HALF,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(S_OPCODE, 0x2), List.of(
      OpType.STORE_WORD,
      OpType.EXEC_POST
    ));

    // B format
    instTrie.put(List.of(B_OPCODE, 0x0), List.of(
      OpType.BRANCH_EQ
    ));
    instTrie.put(List.of(B_OPCODE, 0x1), List.of(
      OpType.BRANCH_NE
    ));
    instTrie.put(List.of(B_OPCODE, 0x4), List.of(
      OpType.BRANCH_LT
    ));
    instTrie.put(List.of(B_OPCODE, 0x5), List.of(
      OpType.BRANCH_GE
    ));
    instTrie.put(List.of(B_OPCODE, 0x6), List.of(
      OpType.BRANCH_LTU
    ));
    instTrie.put(List.of(B_OPCODE, 0x7), List.of(
      OpType.BRANCH_GEU
    ));

    // J format
    instTrie.put(List.of(J_OPCODE), List.of(
      OpType.JAL
    ));

    // I format (jump)
    instTrie.put(List.of(IJ_OPCODE, 0x0), List.of(
      OpType.JAL_REG
    ));

    // U format (load, add)
    instTrie.put(List.of(UL_OPCODE), List.of(
      OpType.LUI,
      OpType.EXEC_POST
    ));
    instTrie.put(List.of(UA_OPCODE), List.of(
      OpType.AUIPC,
      OpType.EXEC_POST
    ));

    // I format (environment)
    instTrie.put(List.of(IE_OPCODE, 0x0), List.of(
      OpType.ENV,
      OpType.EXEC_POST
    ));
  }

  /**
   * Decodes an instruction into a sequence of microops, and queues them to a processor instance.
   *
   * @param proc processor instance
   * @param inst instruction to decode
   */
  public static void decode(Processor proc, int inst) {
    // get opcode, funct3, funct7 keys to query trie
    int opcode = opcode(inst);
    int funct3 = funct3(inst);
    int funct7 = funct7(inst);

    // get microop
    List<OpType> opList = instTrie.get(List.of(opcode, funct3, funct7));
    if (opList == null) {
      throw new RuntimeException("Unknown instruction " + DebugShell.int32ToString(inst));
    }

    // iterate completing microops and pushing to processor queue
    for (OpType opType : opList) {
      MicroOp op = new MicroOp(opType, inst);
      proc.opQueue.add(op);
    }
  }
}
