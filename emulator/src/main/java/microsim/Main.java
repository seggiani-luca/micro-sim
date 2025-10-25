package microsim;

import java.io.IOException;
import microsim.ui.*;
import microsim.simulation.*;
import microsim.simulation.component.device.keyboard.*;
import microsim.MainEnvironment.SimulationInfo;

/**
 * Contains program entry point. Loads EPROM data and instantiates simulations based on them.
 */
public class Main {

  /**
   * Version of emulator.
   */
  public static final String VERSION;

  /**
   * Year. <!-- TODO --> For now manually set, might eventually want to read it from MANIFEST.
   */
  public static final String YEAR = "2025";

  // get version from MANIFEST, if present
  static {
    Package pkg = Main.class.getPackage();
    VERSION = (pkg != null && pkg.getImplementationVersion() != null)
            ? pkg.getImplementationVersion()
            : "DEV_BUILD";
  }

  /**
   * Main environment, to be instantiated on startup.
   */
  private static MainEnvironment env;

  /**
   * Shows project name, version, year and authorship.
   */
  private static void greet() {
    System.out.println("micro-sim emulator " + VERSION);
    System.out.println(YEAR + " - Luca Seggiani\n");
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
    VideoWindow window = new VideoWindow(simulation.video, env.windowScale,
            simulation.getSimulationName());
    simulation.addListener(window);

    // 2. handle debug shell. should be always attached as processor might activate it on EBREAK
    System.out.println("Attaching debug shell");
    DebugShell debugShell = new DebugShell();
    debugShell.attachSimulation(simulation);

    // activate if requested
    if (env.debugMode) {
      System.out.println("Debug mode requested, activating debug shell");

      // activate debug shell
      debugShell.activate();
    }

    // 3. handle keyboard
    System.out.println("Attaching keyboard source");
    KeyboardSource keyboardSource = new KeyboardSource(window.getPanel());
    simulation.keyboard.attachSource(keyboardSource);
  }

  /**
   * Instantiates a simulation. Initialization flow is:
   * <ol>
   * <li>Instantiate a {@link microsim.simulation.Simulation} object with said configuration and
   * data.</li>
   * <li>Load EPROM data into simulation memory.</li>
   * <li>Attach interfaces to simulation. These include {@link ui.VideoWindow},
   * {@link ui.DebugShell}, and keyboard source for the keyboard device. Attachment is handled by
   * the {@link #initInterfaces(microsim.simulation.Simulation)} method.</li>
   * <li>Begin executing simulation.</li>
   * </ol>
   *
   * @param epromData EPROM data to load into memory
   * @param simulationName name of simulated simulation
   */
  public static void initSimulation(String simulationName, byte[] epromData) {
    System.out.println(">> Initializing simulation: \"" + simulationName + "\"");

    // 1. initialize simulation
    Simulation simulation = new Simulation(simulationName);

    // 2. load EPROM
    simulation.memory.loadEPROM(epromData);

    // 3. initialize interfaces: video window, debug shell and keyboard
    try {
      initInterfaces(simulation);
    } catch (RuntimeException e) {
      System.err.println("Couldn't initialize interfaces. " + e.getMessage());
      System.exit(1);
    }

    // 4. begin simulation
    System.out.println("Simulation: \"" + simulationName + "\" powering on\n");
    simulation.begin();
  }

  /**
   * Program entry point. Main program flow is:
   * <ol>
   * <li>Use {@link microsim.MainEnvironment} to parse arguments and instantiate a list of
   * simulation info objects.</li>
   * <li>Instantiate these simulation info objects into actual simulations, attach interfaces to
   * them, and execute them. Instantiation is handled by
   * {@link #initSimulation(java.lang.String, byte[])}.</li>
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

    // 2. instantiate simulations from simulation infos in main environment
    for (SimulationInfo info : env.simulationInfos) {
      initSimulation(info.simulationName, info.epromData);
    }
  }
}
