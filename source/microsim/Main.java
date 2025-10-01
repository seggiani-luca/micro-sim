package microsim;

import java.io.*;
import microsim.simulation.*;
import microsim.simulation.component.*;
import microsim.ui.*;

/**
 * Contains program entry point. Handles program arguments and EPROM loading from .dat files,
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
   * Opens .dat file containing EPROM data and returns it as a byte array. Each line in the .dat
   * file is expected to contain a 16 byte word formatted as XX XX. Trailing comments preceded by
   * "//" are ignored.
   *
   * @param path the path of the .dat file
   * @return a byte array that contains EPROM data
   * @throws IOException if the .dat file can't be opened
   */
  static byte[] loadEpromData(String path) throws IOException {
    // always print EPROM file path
    System.out.println("Reading EPROM from " + path);

    // initialize EPROM data array
    byte[] epromData = new byte[MAX_EPROM_SIZE];
    int idx = 0;

    // open EPROM data file
    BufferedReader reader = new BufferedReader(new FileReader(path));
    String line;

    // read by line
    while ((line = reader.readLine()) != null) {
      line = line.strip();
      if (line.isEmpty()) {
        continue; // empty line
      }
      // read by token
      String[] tokens = line.split("\\s+"); // split by whitespace
      for (String token : tokens) {
        if (token.equals("//")) {
          break; // comment
        }
        // parse byte and insert
        byte value = (byte) Integer.parseInt(token, 16);
        epromData[idx++] = value;

        // check bounds
        if (idx >= MAX_EPROM_SIZE) {
          System.out.println("Ignoring rest of EPROM data file, buffer full");
          break;
        }
      }
    }

    // close EPROM data file
    reader.close();

    return epromData;
  }

  /**
   * Program entry point. The program flow is:
   * <ol>
   * <li>Get data needed for simulation instantiation. This includes the .dat file containing EPROM
   * data and the debug argument.</li>
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
    // 1) read EPROM .dat file
    // get .dat flile path argument
    String epromDataPath = getArgument(args, "-e");
    if (epromDataPath == null) {
      System.out.println("Please specify an EPROM data path with argument -e <eprom_data_path>");
      System.exit(1);
    }

    // load EPROM data in byte array
    byte[] epromData = null;
    try {
      epromData = loadEpromData(epromDataPath);
    } catch (IOException | NumberFormatException e) {
      System.out.println("Error loading EPROM data: " + e.getMessage());
      System.exit(1);
    }

    // 2) instantiate simulation
    Simulation simulation = new Simulation(epromData);

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
    simulation.addListener(window);

    // get debug options
    boolean debugMode = getIfArgument(args, "-d");

    // if debugging, attach DebugShell
    DebugShell debugShell = new DebugShell();
    if (debugMode) {
      debugShell.attachSimulation(simulation);
    }

    // 4) begin simulation
    try {
      simulation.run();
    } catch (RuntimeException e) {
      System.out.println("Simulation error: " + e.getMessage());
      System.exit(2);
    }
  }
}
