package microsim.simulation.component.processor;

@FunctionalInterface
interface MicroOp {

  void execute(Processor proc);

}
