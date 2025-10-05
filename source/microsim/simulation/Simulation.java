package microsim.simulation;

import microsim.simulation.component.MemorySpace;
import microsim.simulation.component.processor.*;
import microsim.simulation.component.*;
import microsim.simulation.event.*;
import microsim.ui.*;

/**
 * Represents a simulation instance. Contains references to simulated components and timing
 * constants. Pipes {@link microsim.simulation.event.FrameEvent} events from
 * {@link microsim.simulation.component.SimulationComponent} components to external
 * {@link microsim.simulation.event.SimulationListener} listeners.
 */
public class Simulation extends SimulationComponent implements SimulationListener {

  /**
   * Simulated bus component. Instantiated first, all components connect to it.
   */
  public Bus bus;

  /**
   * Simulated processor component. Instantiated after bus, updated first in simulation cycles.
   */
  public Processor proc;

  /**
   * Simulated memory component. Instantiated after bus, updated second in simulation cycles.
   */
  public MemorySpace memory;

  /**
   * Simulated video device component. Instantiated after bus, updated third in simulation cycles.
   */
  public VideoDevice video;

  /**
   * Video refresh frequency in Hertz.
   */
  static final int VIDEO_FREQ = 60;

  /**
   * CPU clock frequency in Hertz.
   */
  static final int CPU_FREQ = 1000000; // 1 MHz

  /**
   * Frame update period in milliseconds.
   */
  static final int FRAME_TIME = 1000 / VIDEO_FREQ;

  /**
   * Number of CPU clock cycles per frame.
   */
  static final int CYCLES_PER_FRAME = CPU_FREQ / VIDEO_FREQ;

  /**
   * Instantiates simulation, loading EPROM data in memory. Sets self as listener to the simulation
   * components involved.
   *
   * @param epromData data to load in memory
   */
  public Simulation(byte[] epromData) {
    // init bus
    bus = new Bus();

    // init components on bus
    proc = new Processor(bus);
    memory = new MemorySpace(bus, epromData);
    video = new VideoDevice(bus, memory);

    // set as listener
    bus.addListener(this);
    proc.addListener(this);
    memory.addListener(this);
    video.addListener(this);
  }

  /**
   * Pipes {@link microsim.simulation.event.FrameEvent} events from
   * {@link microsim.simulation.component.SimulationComponent} components to external
   * {@link microsim.simulation.event.SimulationListener} listeners.
   *
   * @param e event to pipe
   */
  @Override
  public void onSimulationEvent(SimulationEvent e) {
    raiseEvent(e);
  }

  /**
   * Performs a simulation cycle. A cycle is performed by:
   * <ol>
   * <li>Stepping the bus to propagate buffered values.</li>
   * <li>Stepping components. Components are stepped in order:
   * <ol>
   * <li>Processor.</li>
   * <li>Memory space.</li>
   * <li>I/O devices.</li>
   * </ol>
   * </li>
   * </ol>
   */
  @Override
  public void step() {
    // bus lines take their value
    bus.step();

    // components read and step
    proc.step();
    memory.step();
    video.step();
  }

  /**
   * Executes simulation. All components on bus do {@link #CYCLES_PER_FRAME} cycles per frame, and
   * at the end of frames video updates, at {@link #VIDEO_FREQ} Hz.
   */
  public void run() {
    // init cycle counters
    long cycle = 1;
    long nextFrameCycle = CYCLES_PER_FRAME;

    // enter simulation loop
    while (true) {
      // step through simulation cycles
      while (cycle < nextFrameCycle) {
        raiseEvent(new CycleEvent(this, cycle));

        // actually perform simulation
        step();

        cycle++;
      }

      // perform video update
      video.render();

      // set next video update cycle
      nextFrameCycle = cycle + CYCLES_PER_FRAME;

      // sleep until next video update
      try {
        Thread.sleep(FRAME_TIME);
      } catch (InterruptedException e) {
        System.out.println("Simulation interrupted");
        break;
      }
    }
  }

}
