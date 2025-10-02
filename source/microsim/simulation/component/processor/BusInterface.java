package microsim.simulation.component.processor;

import microsim.simulation.component.Bus;
import microsim.simulation.component.Bus.BYTE_SELECT;

public class BusInterface {

  public static void doReadRoutine(Processor proc, int addr, BYTE_SELECT byteSelect) {
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);

    readRoutine(proc);
  }

  private static void readRoutine(Processor proc) {
    proc.opQueue.add(
      (cpu) -> {
        cpu.bus.readEnable.drive(cpu, true);
      }
    );
    proc.opQueue.add(
      (cpu) -> {
        cpu.bus.readEnable.drive(cpu, false);
      }
    );
    proc.opQueue.add(
      (cpu) -> {
        proc.temp = cpu.bus.dataLine.read();
      }
    );
  }

  public static void doWriteRoutine(
    Processor proc, int addr, int data, Bus.BYTE_SELECT byteSelect) {
    proc.temp = data;
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);

    writeRoutine(proc);
  }

  private static void writeRoutine(Processor proc) {
    proc.opQueue.add(
      (cpu) -> {
        cpu.bus.dataLine.drive(cpu, proc.temp);
      }
    );
    proc.opQueue.add(
      (cpu) -> {
        cpu.bus.writeEnable.drive(cpu, true);
      }
    );
    proc.opQueue.add(
      (cpu) -> {
        cpu.bus.writeEnable.drive(cpu, false);
      }
    );
    proc.opQueue.add(
      (cpu) -> {
        cpu.bus.dataLine.release(cpu);
      }
    );
  }
}
