package microsim.simulation;

import microsim.simulation.component.*;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.processor.*;
import microsim.simulation.component.memory.*;
import microsim.simulation.component.device.video.*;
import microsim.simulation.component.device.keyboard.*;
import microsim.simulation.component.device.timer.*;
import microsim.simulation.event.*;
import microsim.ui.DebugShell;

/**
 * Represents a simulation instance by keeping references to simulated components. Pipes
 * {@link microsim.simulation.event.FrameEvent} events from
 * {@link microsim.simulation.component.SimulationComponent} components to external
 * {@link microsim.simulation.event.SimulationListener} listeners (usually interfaces).
 */
public class Simulation extends SimulationComponent implements SimulationListener {

  /**
   * Base address of video device.
   */
  public static final int VIDEO_BASE = 0x00030000;

  /**
   * Base address of keyboard device.
   */
  public static final int KEYBOARD_BASE = 0x00040000;

  /**
   * Base address of timer device.
   */
  public static final int TIMER_BASE = 0x00050000;

  /**
   * Simulated processor component.
   */
  public final Processor proc;

  /**
   * Simulated memory component.
   */
  public final MemorySpace memory;

  /**
   * Simulated video device component.
   */
  public final VideoDevice video;

  /**
   * Simulated keyboard device component.
   */
  public final KeyboardDevice keyboard;

  /**
   * Simulated timer device component.
   */
  public final TimerDevice timer;

  /**
   * Is the simulation running?
   */
  private volatile boolean running = true;

  /**
   * Instantiates simulation, loading EPROM data in memory and configuring devices and components.
   * Sets self as listener of the simulation components involved.
   *
   * @param simulationName name of this simulation
   */
  @SuppressWarnings("LeakingThisInConstructor")
  public Simulation(String simulationName) {
    // simulation instances don't attach to buses (they instead own one)
    super(null, simulationName);

    // init bus
    bus = new Bus(simulationName);

    // init components on bus
    proc = new Processor(bus, simulationName);
    memory = new MemorySpace(bus, simulationName);
    video = new VideoDevice(bus, VIDEO_BASE, simulationName);
    keyboard = new KeyboardDevice(bus, KEYBOARD_BASE, simulationName);
    timer = new TimerDevice(bus, TIMER_BASE, simulationName);

    // attach memory to video
    video.attachMemory(memory);

    // set self as listener
    // this leaks a this reference but we don't expect listeners to use it before event is raised
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
   * {@link microsim.simulation.event.SimulationListener} listeners. Checks for
   * {@link microsim.simulation.event.HaltEvent} events to power off.
   *
   * @param e event to pipe
   */
  @Override
  public void onSimulationEvent(SimulationEvent e) {
    if (e instanceof HaltEvent) {
      poweroff();
    }

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
    proc.step();
    memory.step();
    video.step();
    keyboard.step();
    timer.step();
  }

  /**
   * Main simulation thread.
   */
  private void mainThread() {
    // init cycle counter
    long cycle = 0;

    // enter simulation loop
    while (running) {
      // if debugging signal cycle to show debug shell
      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new CycleEvent(this, cycle));
      }

      // actually perform simulation step
      step();

      // increase cycle
      cycle++;
    }

    System.out.println("Simulation: \"" + simulationName + "\" powering off\n");
  }

  /**
   * Executes simulation. All components on local bus update as fast as possible. Threaded devices
   * (like video and timer) run on separate threads at fixed frequency.
   */
  public void begin() {
    // start other threads
    video.begin();
    timer.begin();

    // start main simulation thread
    Thread simulationThread = new Thread(() -> mainThread());
    simulationThread.setName(simulationName + ": Main");
    simulationThread.start();
  }

  /**
   * Stops execution of simulation. Threaded devices are stopped before the simulation.
   */
  public void poweroff() {
    // stop other threads
    video.stop();
    timer.stop();

    // stop this thread
    running = false;
  }
}
