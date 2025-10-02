package microsim.simulation.component.processor;

public class Decoder {

  static class RType {

    int funct;
    int rs1;
    int rs2;
    int rd;

    public RType(int inst) {
      int funct3 = (inst >> 12) & 0x3;
      int funct7 = (inst >> 25);

      // pack funct
      funct = (funct7 << 16) | funct3;

      rs1 = (inst >> 15) & 0x1f;
      rs2 = (inst >> 20) & 0x1f;
      rd = (inst >> 7) & 0x1f;
    }

    public void decode(Processor proc) {

      switch (funct) {
        case (0x00 << 16) | 0x0: {
          // add
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = cpu.registers[rs1] + cpu.registers[rs2];
            }
          );

          break;
        }
        case (0x20 << 16) | 0x0: {
          // sub
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = cpu.registers[rs1] - cpu.registers[rs2];
            }
          );

          break;
        }
        case (0x00 << 16) | 0x4: {
          // xor
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = cpu.registers[rs1] ^ cpu.registers[rs2];
            }
          );

          break;
        }
        case (0x00 << 16) | 0x6: {
          // or
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = cpu.registers[rs1] | cpu.registers[rs2];
            }
          );

          break;
        }
        case (0x00 << 16) | 0x7: {
          // and
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = cpu.registers[rs1] & cpu.registers[rs2];
            }
          );

          break;
        }
        case (0x00 << 16) | 0x1: {
          // sll
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = cpu.registers[rs1] << cpu.registers[rs2];
            }
          );

          break;
        }
        case (0x00 << 16) | 0x5: {
          // srl
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = cpu.registers[rs1] >>> cpu.registers[rs2];
            }
          );

          break;
        }
        case (0x20 << 16) | 0x5: {
          // sra
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = cpu.registers[rs1] >> cpu.registers[rs2];
            }
          );

          break;
        }
        case (0x00 << 16) | 0x2: {
          // slt
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd] = (cpu.registers[rs1] < cpu.registers[rs2]) ? 1 : 0;
            }
          );

          break;
        }
        case (0x00 << 16) | 0x3: {
          // sltu
          proc.opQueue.add(
            (cpu) -> {
              cpu.registers[rd]
              = (Integer.compareUnsigned(cpu.registers[rs1], cpu.registers[rs2]) < 0) ? 1 : 0;
            }
          );

          break;
        }
      }
    }
  }

  public static void decode(Processor proc, int inst) {
    int opcode = inst & 0x7f;

    switch (opcode) {
      case 0x33: {
        // R
        RType r = new RType(inst);
        r.decode(proc);

        break;
      }

    }
  }
}
