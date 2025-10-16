package microsim;

import microsim.simulation.info.SimulationInfo;
import java.io.IOException;
import microsim.simulation.*;
import microsim.simulation.component.device.keyboard.*;
import microsim.ui.*;

/**
 * Contains program entry point. Handles program arguments and EPROM loading, instantiates
 * simulation instances and attaches interfaces.
 */
public class Main {

  /**
   * Version of emulator.
   */
  public static final String VERSION;
  public static final String YEAR = "2025";

  // get version
  static {
    Package pkg = Main.class.getPackage();
    VERSION = pkg.getImplementationVersion();
  }

  /**
   * Shows version and other basic info.
   */
  private static void greet() {
    System.out.println("micro-sim emulator " + VERSION);
    System.out.println(YEAR + " - Luca Seggiani\n");
  }

  /**
   * Initializes interfaces and attaches them to simulation
   *
   * @param simulation simulation instance to attach to
   * @param info main info containing info about interfaces
   */
  private static void initInterfaces(Simulation simulation, MainInfo info) {
    System.out.println("Initializing video window");

    // instantiate video window and attach it
    VideoWindow window = new VideoWindow(info.windowScale);
    simulation.addListener(window);

    // instantiate debug shell and attach it
    DebugShell debugShell = new DebugShell();
    debugShell.attachSimulation(simulation);

    if (info.debugMode) {
      System.out.println("Debug mode requested, activating shell");
      debugShell.activate();
    }

    // attach keyboard
    System.out.println("Attaching keyboard to window panel");
    JComponentKeyboardSource keyboardSource = new JComponentKeyboardSource(window.getPanel());
    simulation.keyboard.attachSource(keyboardSource);
  }

  /**
   * Program entry point. The program flow is:
   * <ol>
   * <li>Get data needed for simulation instantiation. This includes arguments, configuration files,
   * and the object file containing EPROM data.</li>
   * <li>Instantiate a {@link microsim.simulation.Simulation} object with said configuration and
   * data.</li>
   * <li>Attach interfaces to simulation. These include {@link ui.VideoWindow} and
   * {@link ui.DebugShell}.</li>
   * <li>Begin executing simulation.</li>
   * </ol>
   *
   * Return values are:
   * <ol>
   * <li>Normal termination.</li>
   * <li>User error.</li>
   * <li>Simulation error.</li>
   * </ol>
   *
   * @param args program arguments
   */
  public static void main(String[] args) {
    greet();

    // 1. get data
    MainInfo info = null;
    try {
      info = new MainInfo(args);
    } catch (IOException e) {
      System.err.println("Couldn't initialize program. " + e.getMessage());
    }

    // 2. initialize simulation
    SimulationInfo simulationInfo = new SimulationInfo(
            info.epromData,
            info.sConfig
    );
    Simulation simulation = new Simulation(simulationInfo);

    // 3. initialize interfaces: video window, debug shell
    initInterfaces(simulation, info);

    // 4. begin simulation
    try {
      System.out.println("Simulation powering on");
      simulation.begin();
    } catch (RuntimeException e) {
      System.err.println("Simulation error. " + e.getMessage());
      e.printStackTrace(); // for now print stacktrace
      System.exit(2);
    }
  }
}
