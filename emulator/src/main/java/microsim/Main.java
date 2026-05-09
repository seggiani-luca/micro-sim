package microsim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import microsim.ui.*;
import microsim.simulation.*;
import microsim.simulation.component.device.keyboard.*;
import microsim.MainEnvironment.SimulationInfo;
import microsim.file.IMG;

/**
 * Contains program entry point. Loads EPROM data and instantiates simulations based on them.
 */
public class Main {

  /**
   * Hide constructor.
   */
  private Main() {
  }

  /**
   * Version of emulator.
   */
  public static final String VERSION;

  // get version from MANIFEST, if present
  static {
    // get package manifest
    Package pkg = Main.class.getPackage();

    // get version
    VERSION = (pkg != null && pkg.getImplementationVersion() != null)
            ? pkg.getImplementationVersion()
            : "DEV_BUILD";
  }

  /**
   * Main environment, to be instantiated on startup.
   */
  private static MainEnvironment env;

  /**
   * Debug shell used to debug all simulation instances.
   */
  private static DebugShell debugShell;

  /**
   * Global array of instantiated simulation instances.
   */
  private static final List<Simulation> simulationInstances = new ArrayList<>();

  /**
   * Shows project name, version and authorship.
   */
  private static void greet() {
    System.out.println("micro-sim emulator " + VERSION + " - Luca Seggiani");
  }

  /**
   * Initializes interfaces and attaches them to the given simulation.
   *
   * Handled interfaces are:
   * <ol>
   * <li>Video window.</li>
   * <li>Debug shell, always attached, activated if debug mode is requested (through argument).</li>
   * <li>Keyboard: attached and listening from video window.</li>
   * </ol>
   *
   * @param simulation simulation instance to attach interfaces to
   */
  private static void initInterfaces(Simulation simulation) {
    // 1. handle video window
    System.out.println("Initializing video window");
    VideoWindow window = new VideoWindow(simulation.video, env.windowScale, simulation.name);
    simulation.addListener(window);

    // 2. handle debug shell. should be always attached as processor might activate it on EBREAK
    System.out.println("Attaching debug shell");
    debugShell.attachSimulation(simulation);

    // activate if requested
    if (env.debugMode) {
      System.out.println("Debug mode requested, activating debug shell");

      // activate debug shell
      debugShell.activate();
    }

    // 3. handle keyboard
    System.out.println("Attaching keyboard source");
    KeyboardSource keyboardSource = new KeyboardSource(window.getPanel(), KeyboardSource.set1);
    simulation.keyboard.attachSource(keyboardSource);

    System.out.println();
  }

  /**
   * Instantiates a simulation. Initialization flow is:
   * <ol>
   * <li>Instantiate a {@link microsim.simulation.Simulation} object with said configuration and
   * data.</li>
   * <li>Load EPROM and disk data into simulation memory.</li>
   * <li>Attach interfaces to simulation. These include {@link microsim.ui.VideoWindow},
   * {@link microsim.ui.DebugShell}, and keyboard source for the keyboard device. Attachment is
   * handled by the {@link #initInterfaces(microsim.simulation.Simulation)} method.</li>
   * </ol>
   *
   * @param info simulation info, with EPROM data to load into memory and name
   * @return simulation that was instantiated
   */
  public static Simulation initSimulation(SimulationInfo info) {
    System.out.println(">> Initializing simulation: \"" + info.simulationName + "\"");

    // 1. initialize simulation
    Simulation simulation = new Simulation(info.simulationName);

    // 2. load EPROM and disk
    simulation.memory.loadEPROM(info.epromData);
    if (info.diskImage != null) {
      simulation.disk.loadDisk(info.diskImage);
    }

    // 3. initialize interfaces: video window, debug shell and keyboard
    try {
      initInterfaces(simulation);
    } catch (RuntimeException e) {
      System.err.println("Couldn't initialize interfaces. " + e.getMessage());
      System.exit(1);
    }

    return simulation;
  }

  /**
   * Program entry point. Main program flow is:
   * <ol>
   * <li>Use {@link microsim.MainEnvironment} to parse arguments and instantiate a list of
   * simulation info objects.</li>
   * <li>Instantiate these simulation info objects into actual simulations, attach interfaces to
   * them, and execute them. Instantiation is handled by
   * {@link #initSimulation(microsim.MainEnvironment.SimulationInfo)}.</li>
   * <li>Begin simulations.</li>
   * <li>Wait for simulations, storing state at the end (mainly disk images).</li>
   * </ol>
   * Any simulation instantiation failure aborts the entire program.
   *
   * Return values for the program are:
   * <ol>
   * <li>Normal termination.</li>
   * <li>User error. Configuration and file I/O errors are considered user errors and return this
   * code.</li>
   * <li>Simulation error. These are issued when something actually breaks in the simulated
   * environment.</li>
   * </ol>
   *
   * @param args program arguments
   */
  @SuppressWarnings("UnusedAssignment")
  public static void main(String[] args) {
    greet();

    // 1. get environment. this includes loading simulation EPROMs and other arguments
    try {
      env = new MainEnvironment(args);
    } catch (IOException e) {
      System.err.println("Couldn't initialize program. " + e.getMessage());
      System.exit(1);
    }

    System.out.println("Loaded " + env.simulationInfos.size() + " simulation EPROM(s)\n");

    // 2. instantiate simulations from simulation infos in main environment, and attach interfaces
    debugShell = DebugShell.getInstance(); // has to be unique
    for (SimulationInfo info : env.simulationInfos) {
      Simulation simulation = initSimulation(info);
      simulationInstances.add(simulation);
    }


    // 3. begin simulations
    List<Thread> simulationThreads = new LinkedList<>();
    for (Simulation simulation : simulationInstances) {
      System.out.println(">> Simulation \"" + simulation.name + "\" powering on\n");
      simulationThreads.add(simulation.begin());
    }

    // 4. wait and store state
    for (Thread thread : simulationThreads) {
      try {
        thread.join();
      } catch (InterruptedException ex) {
        throw new RuntimeException("Main thread was interrupted while waiting for simulations.");
      }
    }

    System.out.println(">> Syncing state...");
    for (Simulation simulation : simulationInstances) {
      try {
        // write simulation disk image
        IMG.writeIMG(simulation.name, env.diskPath, simulation.disk.getStorage());
      } catch (IOException ex) {
        System.err.println("Failed to write disk image for simulation " + simulation.name);
      }
    }
  }
}
