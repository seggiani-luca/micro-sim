package microsim;

import java.io.IOException;
import microsim.simulation.component.*;
import microsim.elf.*;
import microsim.simulation.*;
import microsim.ui.*;

/**
 * Contains program entry point. Handles program arguments and EPROM loading from object files,
 * instantiates simulation instances and attaches interfaces.
 */
public class Main {

  /**
   * The maximum size, in bytes, of EPROM data. This is set to EPROM_END - EPROM_BEG from
   * {@link microsim.simulation.component.MemorySpace}.
   */
  static final int MAX_EPROM_SIZE = MemorySpace.EPROM_END - MemorySpace.EPROM_BEG;

  /**
   * Gets argument parameter following argument tag.
   *
   * @param args program's argument string Array
   * @param tag the argument tag to search for (such as "-d")
   * @return the argument parameter as a string
   */
  static String getArgument(String[] args, String tag) {
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
   * Gets if argument tag is present (used for Boolean arguments).
   *
   * @param args program's argument string Array
   * @param tag the argument tag to search for (such as "-d")
   * @return a Boolean that represents if the tag was found
   */
  static boolean getIfArgument(String[] args, String tag) {
    // step through arguments until tag is found
    for (String arg : args) {
      if (tag.equals(arg)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Program entry point. The program flow is:
   * <ol>
   * <li>Get data needed for simulation instantiation. This includes the object file containing
   * EPROM data and the debug argument.</li>
   * <li>Instantiate a {@link microsim.simulation.Simulation} object with said data.</li>
   * <li>Attach interfaces to simulation. These include {@link microsim.ui.VideoWindow} and
   * {@link microsim.ui.DebugShell}.</li>
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
    // 1) read object file
    // get object path argument
    String epromDataPath = getArgument(args, "-e");
    if (epromDataPath == null) {
      System.out.println("Please specify an object data path with argument -e <object_data_path>");
      System.exit(1);
    }

    try {
      // load object data
      Elf elf = Elf.readELF(epromDataPath);

      System.exit(0);

      // read object data segments into EPROM array
      // byte[] epromData = elf.getEPROM();
    } catch (IOException e) {
      System.out.println("Object data couldn't be read. " + e.getMessage());
      System.exit(1);
    }

    // 2) instantiate simulation
    // Simulation simulation = new Simulation(epromData);
    // 3) init interfaces
    // get graphical window scale
    int windowScale = 1;

    // get scale if given
    String scaleArgument = getArgument(args, "-s");
    if (scaleArgument != null) {
      try {
        windowScale = Integer.parseInt(scaleArgument);
      } catch (NumberFormatException e) {
        System.out.println("Ignoring scale argument " + scaleArgument);
      }
    }

    // instantiate video window and attach it
    VideoWindow window = new VideoWindow(windowScale);
    // simulation.addListener(window);

    // get debug options
    boolean debugMode = getIfArgument(args, "-d");

    // if debugging, attach DebugShell
    DebugShell debugShell = new DebugShell();
    if (debugMode) {
      // debugShell.attachSimulation(simulation);
    }

    // 4) begin simulation
    try {
      // simulation.run();
    } catch (RuntimeException e) {
      System.out.println("Simulation error: " + e.getMessage());
      System.exit(2);
    }
  }
}
