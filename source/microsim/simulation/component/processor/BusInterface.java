package microsim.simulation.component.processor;

import microsim.simulation.component.Bus;
import microsim.simulation.component.Bus.ByteSelect;
import microsim.simulation.component.processor.MicroOp.OpType;

/**
 * Implements an interface a {@link microsim.simulation.component.processor.Processor} instance can
 * use to read and write from the bus it's mounted on.
 */
public class BusInterface {

  /**
   * Starts a read routine.
   *
   * @param proc processor instance that reads
   * @param addr address to read at
   * @param byteSelect format to read
   */
  public static void doReadRoutine(Processor proc, int addr, ByteSelect byteSelect) {
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);

    readRoutine(proc);
  }

  /**
   * Emits sequence of microop to perform a read on a processor queue.
   *
   * @param proc processor instance that reads
   */
  private static void readRoutine(Processor proc) {
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_READ0));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_READ1));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_READ2));
  }

  /**
   * Starts a write routine.
   *
   * @param proc processor instance that writes
   * @param addr address to write at
   * @param byteSelect format to write
   */
  public static void doWriteRoutine(
    Processor proc, int addr, int data, Bus.ByteSelect byteSelect) {
    proc.temp = data;
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);

    writeRoutine(proc);
  }

  /**
   * Emits sequence of microop to perform a write on a processor queue.
   *
   * @param proc processor instance that writes
   */
  private static void writeRoutine(Processor proc) {
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_WRITE0));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_WRITE1));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_WRITE2));
    proc.opQueue.addFirst(new MicroOp(OpType.MEM_WRITE3));

  }
}
