package microsim.simulation.component.processor;

import microsim.simulation.component.Bus;
import microsim.simulation.component.Bus.BYTE_SELECT;
import microsim.simulation.component.processor.MicroOp.OpType;

public class BusInterface {

  public static void doReadRoutine(Processor proc, int addr, BYTE_SELECT byteSelect) {
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);

    readRoutine(proc);
  }

  private static void readRoutine(Processor proc) {
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_READ0));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_READ1));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_READ2));
  }

  public static void doWriteRoutine(
    Processor proc, int addr, int data, Bus.BYTE_SELECT byteSelect) {
    proc.temp = data;
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);

    writeRoutine(proc);
  }

  private static void writeRoutine(Processor proc) {
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_WRITE0));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_WRITE1));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_WRITE2));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_WRITE3));

  }
}
