package microsim;

import java.io.*;
import microsim.elf.Elf;
import microsim.simulation.Simulation;
import microsim.simulation.component.device.keyboard.JComponentKeyboardSource;
import microsim.ui.DebugShell;
import microsim.ui.VideoWindow;

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
   * Gets argument parameter following argument tag. With tag = "-t", from '-t "arg"' returns "arg".
   * If no argument is found, returns null.
   *
   * @param args program's argument string Array
   * @param tag the argument tag to search for (such as "-d")
   * @return the argument parameter as a string
   */
  private static String getArgument(String[] args, String tag) {
    // step through arguments until tag is found
    for (int i = 0; i < args.length; i++) {
      if (tag.equals(args[i])) {
        // is the next argument in bounds?
        if (i + 1 >= args.length) {
          return null;
        } else {
          // return argument
          return args[i + 1];
        }
      }
    }

    return null;
  }

  /**
   * Gets if argument tag is present (used for Boolean arguments). With tag = "-t", from '-t'
   * returns true. If no argument is found, returns false.
   *
   * @param args program's argument string Array
   * @param tag the argument tag to search for (such as "-d")
   * @return a Boolean that represents if the tag was found
   */
  private static boolean hasArgument(String[] args, String tag) {
    // step through arguments until tag is found
    for (String arg : args) {
      if (tag.equals(arg)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Shows version and other basic info.
   */
  private static void greet() {
    System.out.println("micro-sim simulator " + VERSION);
    System.out.println(YEAR + " - Luca Seggiani\n");
  }

  /**
   * Signals whether a debug shell should be attached to the simulation instance.
   */
  private static boolean debugMode = false;

  /**
   * Integer display scale. If not given defaults to 1.
   */
  private static int windowScale = 1;

  /**
   * EPROM data file path.
   */
  private static String epromDataPath = "data/eprom.elf";

  /**
   * Gets program arguments, initializes flags and loads EPROM data.
   *
   * @param args program arguments
   * @return EPROM data array
   */
  private static byte[] getSimulationData(String[] args) {
    final String DEBUG_TAG = "-d";
    final String SCALE_TAG = "-s";
    final String EPROM_TAG = "-e";

    // get debug mode
    debugMode = hasArgument(args, DEBUG_TAG);

    // get display scale
    String scaleArgument = getArgument(args, SCALE_TAG);
    if (scaleArgument != null) {
      // ignore if not integer
      try {
        windowScale = Integer.parseInt(scaleArgument);
      } catch (NumberFormatException e) {
        System.out.println("Ignoring scale argument " + scaleArgument);
      }
    }

    // get EPROM data path
    String epromDataPathArgument = getArgument(args, EPROM_TAG);
    if (epromDataPathArgument != null) {
      epromDataPath = epromDataPathArgument;
    }

    // load EPROM data
    System.out.println("Reading EPROM data at " + epromDataPath);

    byte[] epromData = null;
    try {
      // read ELF file
      Elf elf = new Elf(epromDataPath, debugMode);

      // read object data segments into EPROM array
      epromData = elf.getEPROM();
    } catch (IOException e) {
      System.err.println("EPROM data couldn't be read. " + e.getMessage());
      System.exit(1);
    }

    // if debugging, print EPROM data
    if (debugMode) {
      // DebugShell.printEPROM(epromData);
    }

    return epromData;
  }

  /**
   * Initializes interfaces and attaches them to simulation
   *
   * @param simulation simulation instance to attach to
   */
  private static void initInterfaces(Simulation simulation) {
    System.out.println("Initializing video window");

    // instantiate video window and attach it
    VideoWindow window = new VideoWindow(windowScale);
    simulation.addListener(window);

    // instantiate debug shell and attach it
    DebugShell debugShell = new DebugShell();
    debugShell.attachSimulation(simulation);

    if (debugMode) {
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
   * <li>Get data needed for simulation instantiation. This includes arguments, and the object file
   * containing EPROM data and the debug argument.</li>
   * <li>Instantiate a {@link microsim.simulation.Simulation} object with said data.</li>
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

    // 1. get arguments: debugMode, windowScale, epromDataPath
    byte[] epromData = getSimulationData(args);

    // 2. instantiate simulation
    Simulation simulation = new Simulation(epromData);

    // 3. initialize interfaces: video window, debug shell
    initInterfaces(simulation);

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
