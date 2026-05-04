package microsim.simulation.component.processor;

import microsim.simulation.component.processor.MicroOp.OpType;
import microsim.simulation.event.*;
import microsim.ui.DebugShell;

/**
 * Implements an interface a {@link microsim.simulation.component.processor.Processor} instance can
 * use to read and write from the bus it's mounted on.
 */
public class BusInterface {


  /**
   * Hide constructor.
   */
  private BusInterface() {
  }

  /**
   * Prefetches bus access operations to have only one global instance of each.
   */
  class BusMicroOps {

    /**
     * Hide constructor.
     */
    private BusMicroOps() {
    }

    /**
     * Step 1 of read routine
     */
    static final MicroOp MEM_READ1 = new MicroOp(OpType.MEM_READ1);

    /**
     * Step 2 of read routine
     */
    static final MicroOp MEM_READ2 = new MicroOp(OpType.MEM_READ2);

    /**
     * Step 1 of write routine
     */
    static final MicroOp MEM_WRITE1 = new MicroOp(OpType.MEM_WRITE1);
  }

  /**
   * Starts a read routine.
   *
   * @param proc processor instance that reads
   * @param addr address to read at
   * @param byteSelect format to read
   */
  public static void doReadRoutine(Processor proc, int addr, int byteSelect) {
    // log beginning of read routine
    if (DebugShell.isDebuggingEnabled()) {
      proc.raiseEvent(new DebugEvent(proc, "Processor started read routine at address "
              + DebugShell.int32ToString(addr)));
    }

    // keep track of byte select
    proc.byteSelect = byteSelect;

    // start driving address and control lines
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);
    proc.bus.readEnable.drive(proc, 1);

    // emit microops
    readRoutine(proc);
  }

  /**
   * Emits sequence of microops to perform a read on a processor queue.
   *
   * @param proc processor instance that reads
   */
  private static void readRoutine(Processor proc) {
    proc.opQueue.addFirst(BusMicroOps.MEM_READ2);
    proc.opQueue.addFirst(BusMicroOps.MEM_READ1);
  }

  /**
   * Starts a write routine.
   *
   * @param proc processor instance that writes
   * @param addr address to write at
   * @param data data to write
   * @param byteSelect format to write
   */
  public static void doWriteRoutine(Processor proc, int addr, int data, int byteSelect) {
    // log beginning of write routine
    if (DebugShell.isDebuggingEnabled()) {
      proc.raiseEvent(new DebugEvent(proc, "Processor started write routine at address "
              + DebugShell.int32ToString(addr) + " of data " + DebugShell.int32ToString(data)));
    }

    // keep track of byte select
    proc.byteSelect = byteSelect;

    // start driving data, address and control lines
    proc.bus.dataLine.drive(proc, data);
    proc.bus.addressLine.drive(proc, addr);
    proc.bus.byteSelect.drive(proc, byteSelect);
    proc.bus.writeEnable.drive(proc, 1);

    // emit microops
    writeRoutine(proc);
  }

  /**
   * Emits sequence of microops to perform a write on a processor queue.
   *
   * @param proc processor instance that writes
   */
  private static void writeRoutine(Processor proc) {
    proc.opQueue.addFirst(BusMicroOps.MEM_WRITE1);
  }
}
