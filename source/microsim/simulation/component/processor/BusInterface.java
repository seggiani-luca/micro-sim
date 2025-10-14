package microsim.simulation.component.processor;

import microsim.simulation.event.*;
import microsim.simulation.component.processor.MicroOp.OpType;
import microsim.ui.DebugShell;

/**
 * Implements an interface a {@link microsim.simulation.component.processor.Processor} instance can
 * use to read and write from the bus it's mounted on.
 */
public class BusInterface {

  /**
   * Prefetches bus access operations to have only one instance of each.
   */
  class BusMicroOps {

    static final MicroOp MEM_READ0 = new MicroOp(OpType.MEM_READ0);
    static final MicroOp MEM_READ1 = new MicroOp(OpType.MEM_READ1);

    static final MicroOp MEM_WRITE0 = new MicroOp(OpType.MEM_WRITE0);

  }

  /**
   * Starts a read routine.
   *
   * @param proc processor instance that reads
   * @param addr address to read at
   * @param byteSelect format to read
   */
  public static void doReadRoutine(Processor proc, int addr, int byteSelect) {
    if (DebugShell.active) {
      proc.raiseEvent(new DebugEvent(proc, "Processor started read routine at address "
        + DebugShell.int32ToString(addr)));
    }

    proc.byteSelect = byteSelect; // keep track

    // start driving address and control lines
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);
    proc.bus.readEnable.drive(proc, 1);

    readRoutine(proc);
  }

  /**
   * Emits sequence of microop to perform a read on a processor queue.
   *
   * @param proc processor instance that reads
   */
  private static void readRoutine(Processor proc) {
//    proc.opQueue.addFirst(BusMicroOps.MEM_READ2);
    proc.opQueue.addFirst(BusMicroOps.MEM_READ1);
    proc.opQueue.addFirst(BusMicroOps.MEM_READ0);
  }

  /**
   * Starts a write routine.
   *
   * @param proc processor instance that writes
   * @param addr address to write at
   * @param data data to write
   * @param byteSelect format to write
   */
  public static void doWriteRoutine(
    Processor proc, int addr, int data, int byteSelect) {
    if (DebugShell.active) {
      proc.raiseEvent(new DebugEvent(proc, "Processor started write routine at address "
        + DebugShell.int32ToString(addr) + " of data " + DebugShell.int32ToString(data)));
    }

    proc.byteSelect = byteSelect; // keep track

    // start driving data, address and control lines
    proc.bus.dataLine.drive(proc, data);
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);
    proc.bus.writeEnable.drive(proc, 1);

    writeRoutine(proc);
  }

  /**
   * Emits sequence of microop to perform a write on a processor queue.
   *
   * @param proc processor instance that writes
   */
  private static void writeRoutine(Processor proc) {
    proc.opQueue.addFirst(BusMicroOps.MEM_WRITE0);

  }
}
