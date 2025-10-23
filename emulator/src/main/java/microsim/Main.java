package microsim;

import java.io.IOException;
import microsim.ui.*;
import microsim.simulation.*;
import microsim.simulation.component.device.keyboard.*;
import microsim.simulation.component.device.video.*;
import microsim.simulation.info.SimulationInfo;

/**
 * Contains program entry point. Relies on {@link microsim.MainEnvironment} for argument parsing and
 * file loading, instantiates simulation instances and attaches interfaces to them.
 */
public class Main {

  /**
   * Version of emulator.
   */
  public static final String VERSION;

  /**
   * Year of build. <!-- TODO --> For now manually set, might eventually want to read it from
   * MANIFEST.
   */
  public static final String YEAR = "2025";

  // get version from MANIFEST
  static {
    Package pkg = Main.class.getPackage();
    VERSION = pkg.getImplementationVersion();
  }

  /**
   * Shows project name, version, year of build and authorship.
   */
  private static void greet() {
    System.out.println("micro-sim emulator " + VERSION);
    System.out.println(YEAR + " - Luca Seggiani\n");
  }

  /**
   * Initializes interfaces and attaches them to the given simulation. Interface configuration is
   * taken from a main environment object.
   *
   * Handled interfaces are:
   * <ol>
   * <li>Video window, instantiated if a valid video device is found and configuration is not
   * headless. Note that only the first mounted video device is attached to the video window,
   * subsequent ones are simulated but silent.</li>
   * <li>Debug shell, instantiated and attached if debug mode is requested (through argument).</li>
   * <li>Keyboard: attached to a keyboard input source based on what's specified by configuration.
   * Multiple keyboard policy is same as multiple video devices.</li>
   * </ol>
   *
   * @param simulation simulation instance to attach interfaces to
   * @param mArgs main environment containing interface configuration
   */
  private static void initInterfaces(Simulation simulation, SimulationInfo info) {
    System.out.println("Initializing video window");

    // 1. handle video window
    VideoWindow window = null;

    // only if not headless
    if (!info.headless) {
      // get the first mounted video device
      VideoDevice video = simulation.getDevice(VideoDevice.class);
      if (video != null) {
        // attach the device
        window = new VideoWindow(video, info.windowScale, info.machineName);
        simulation.addListener(window);
      } else {
        System.out.println("No video device mounted, window not initialized");
      }
    }

    // 2. handle debug shell
    // should be instantiated anyways as processor might attach it on an EBREAK instruction
    DebugShell debugShell = new DebugShell();
    debugShell.attachSimulation(simulation);

    // activate if requested
    if (info.debugMode) {
      System.out.println("Debug mode requested, activating shell");

      // activate debug shell
      debugShell.activate();
    }

    // 3. handle keyboard
    KeyboardDevice keyboard = simulation.getDevice(KeyboardDevice.class);

    // only handle if present
    if (keyboard != null) {
      System.out.println("Attaching keyboard to source: " + info.keyboardSourceType.name());

      switch (info.keyboardSourceType) {
        case window -> {
          // attach to window JPanel, if video window was instantiated
          if (window == null) {
            throw new RuntimeException("Can't connect keyboard to nonexistent window");
          }

          JComponentKeyboardSource keyboardSource = new JComponentKeyboardSource(window.getPanel());
          keyboard.attachSource(keyboardSource);
        }
        case detached -> {
          // don't attach keyboard
        }
        default -> {
          throw new RuntimeException("Unknown keyboard source " + info.keyboardSourceType.name());
        }
      }
    }
  }

  /**
   * Instantiates a simulation. Initialization flow is:
   * <ol>
   * <li>Get data needed for simulation instantiation. This includes arguments, configuration files,
   * and the object file containing EPROM data. Most of this is handled by a main environment object
   * and done by the caller (main())</li>
   * <li>Instantiate a {@link microsim.simulation.Simulation} object with said configuration and
   * data.</li>
   * <li>Attach interfaces to simulation. These might include {@link ui.VideoWindow},
   * {@link ui.DebugShell}, and some keyboard source for the keyboard device if present.</li>
   * <li>Begin executing simulation.</li>
   * </ol>
   *
   * @param info info to instantiate simulation from
   */
  public static void initSimulation(SimulationInfo info) {
    System.out.println("--- Initializing simulation of machine: " + info.machineName + " ---");

    // expect 1. to be done
    // 2. initialize simulation
    Simulation simulation = new Simulation(info);

    // 3. initialize interfaces: video window, debug shell and keyboard
    try {
      initInterfaces(simulation, info);
    } catch (RuntimeException e) {
      System.err.println("Couldn't initialize interfaces. " + e.getMessage());
      System.exit(1);
    }

    // 4. begin simulation
    System.out.println("Simulation of machine " + info.machineName + " powering on\n");
    simulation.begin();
  }

  /**
   * Program entry point. Loads machine configurations and instantiates simulations based on them.
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

    // get environment. this includes loading machine configurations
    MainEnvironment env = null;
    try {
      env = new MainEnvironment(args);
    } catch (IOException e) {
      System.err.println("Couldn't initialize program. " + e.getMessage());
      System.exit(1);
    }

    System.out.println("Loaded " + env.simulationInfos.size() + " machine configuration(s)\n");

    // instantiate simulations from got simulation infos
    for (SimulationInfo info : env.simulationInfos) {
      initSimulation(info);
    }
  }
}
