package microsim;

import java.io.IOException;
import java.util.List;
import microsim.simulation.*;
import microsim.simulation.component.device.*;
import microsim.simulation.component.device.keyboard.*;
import microsim.simulation.component.device.video.*;
import microsim.simulation.info.SimulationInfo;
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
   * Shows version and other basic mArgs.
   */
  private static void greet() {
    System.out.println("micro-sim emulator " + VERSION);
    System.out.println(YEAR + " - Luca Seggiani\n");
  }

  /**
   * Initializes interfaces and attaches them to simulation
   *
   * @param simulation simulation instance to attach to
   * @param mArgs main mArgs containing mArgs about interfaces
   */
  private static void initInterfaces(Simulation simulation, MainEnvironment mArgs) {
    System.out.println("Initializing video window");

    // if requested and video device present, instantiate video window and attach it
    VideoWindow window = null;
    if (!mArgs.headless) {
      VideoDevice video = simulation.getDevice(VideoDevice.class);
      if (video != null) {
        window = new VideoWindow(video, mArgs.windowScale);
        simulation.addListener(window);
      } else {
        System.out.println("No video device mounted, window not initialized");
      }
    }

    // if requested, instantiate debug shell and attach it
    if (mArgs.debugMode) {
      DebugShell debugShell = new DebugShell();
      debugShell.attachSimulation(simulation);
      System.out.println("Debug mode requested, activating shell");
      debugShell.activate();
    }

    // if present attach keyboard
    KeyboardDevice keyboard = simulation.getDevice(KeyboardDevice.class);
    if (keyboard != null) {
      System.out.println("Attaching keyboard to source: " + mArgs.keyboardSource.name());

      switch (mArgs.keyboardSource) {
        case WINDOW -> {
          if (window == null) {
            throw new RuntimeException("Can't connect keyboard to nonexistent window");
          }

          JComponentKeyboardSource keyboardSource = new JComponentKeyboardSource(window.getPanel());
          keyboard.attachSource(keyboardSource);
        }
        default -> {
          throw new RuntimeException("Unknown keyboard source " + mArgs.keyboardSource.name());
        }
      }
    }
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
    MainEnvironment mArgs = null;
    try {
      mArgs = new MainEnvironment(args);
    } catch (IOException e) {
      System.err.println("Couldn't initialize program. " + e.getMessage());
      System.exit(1);
    }

    // 2. initialize simulation
    SimulationInfo simulationInfo = null;
    try {
      simulationInfo = new SimulationInfo(mArgs.epromData, mArgs.sConfig);
    } catch (IOException e) {
      System.err.println("Couldn't initialize simulation. " + e.getMessage());
      System.exit(1);
    }

    Simulation simulation = new Simulation(simulationInfo);

    // 3. initialize interfaces: video window, debug shell
    try {
      initInterfaces(simulation, mArgs);
    } catch (RuntimeException e) {
      System.err.println("Couldn't initialize interfaces. " + e.getMessage());
      System.exit(1);
    }

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
