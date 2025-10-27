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
   * Name of this simulation
   */
  public final String name;

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
   * Returns if the simulation is running.
   *
   * @return state of simulation
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Instantiates simulation, loading EPROM data in memory and configuring devices and components.
   * Sets self as listener of the simulation components involved.
   *
   * @param name name of this simulation
   */
  @SuppressWarnings("LeakingThisInConstructor")
  public Simulation(String name) {
    // simulation instances don't attach to buses (they instead own one)
    // they also don't have a reference to a simulation they belong to (they are one)
    super(null, null);
    simulation = this;

    this.name = name;

    // init bus
    bus = new Bus(this);

    // init components on bus
    proc = new Processor(bus, this);
    memory = new MemorySpace(bus, this);
    video = new VideoDevice(bus, VIDEO_BASE, this);
    keyboard = new KeyboardDevice(bus, KEYBOARD_BASE, this);
    timer = new TimerDevice(bus, TIMER_BASE, this);

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

    System.out.println("\n>> Simulation: \"" + name + "\" powering off\n");
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
    simulationThread.setName(name + ": Main");
    simulationThread.setUncaughtExceptionHandler(new DebugShell.DebugExceptionHandler());
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
