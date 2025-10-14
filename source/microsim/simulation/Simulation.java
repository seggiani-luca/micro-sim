package microsim.simulation;

import microsim.simulation.event.*;
import microsim.simulation.component.*;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.device.video.*;
import microsim.simulation.component.device.keyboard.*;
import microsim.simulation.component.device.timer.*;
import microsim.simulation.component.memory.*;
import microsim.simulation.component.processor.*;
import microsim.ui.DebugShell;

/**
 * Represents a simulation instance. Contains references to simulated components and timing
 * constants. Pipes {@link microsim.simulation.event.FrameEvent} events from
 * {@link microsim.simulation.component.SimulationComponent} components to external
 * {@link microsim.simulation.event.SimulationListener} listeners (usually interfaces).
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
   * Simulated keyboard device component. Instantiated after bus, updated third in simulation.
   * cycles.
   */
  public KeyboardDevice keyboard;

  /**
   * Simulated timer component. Instantiated after bus, updated third in simulation.
   */
  public TimerDevice timer;

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
    video = new VideoDevice(bus, MemorySpace.VIDEO_BASE, memory);
    keyboard = new KeyboardDevice(bus, MemorySpace.KEYBOARD_BASE);
    timer = new TimerDevice(bus, MemorySpace.TIMER_BASE);

    // set as listener. this is leaky but we don't expect listeners to use it before event is raised
    bus.addListener(this);
    proc.addListener(this);
    memory.addListener(this);
    video.addListener(this);
    keyboard.addListener(this);
    timer.addListener(this);
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
  public final void step() {
    // bus lines take their value
    bus.step();

    // components read and step
    // local bus
    proc.step();
    memory.step();

    // devices
    video.step();
    keyboard.step();
    timer.step();
  }

  /**
   * Executes simulation. All components on local bus update as fast as possible. Video and timer
   * run on separate threads at fixed frequency.
   */
  public void begin() {
    // start other threads
    video.begin();
    timer.begin();

    // init cycle counter
    long cycle = 0;

    // enter simulation loop
    while (true) {
      // if debugging signal cycle to show debug shell
      if (DebugShell.active) {
        raiseEvent(new CycleEvent(this, cycle));
      }

      // actually perform simulation step
      step();

      // increase cycle
      cycle++;
    }
  }
}
