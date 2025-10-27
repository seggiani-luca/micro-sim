package microsim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import microsim.file.ELF;

/**
 * Gets and represents info related to the main program flow, including arguments and simulation
 * configuration files. Uses these to build a list of simulation info objects to later instantiate.
 */
public class MainEnvironment {

  /**
   * Represents information about a simulation instance.
   */
  public static class SimulationInfo {

    /**
     * EPROM the instance should load.
     */
    public byte[] epromData;

    /**
     * Name of simulation.
     */
    public String simulationName;

    /**
     * Constructs simulation info from EPROM data and name.
     *
     * @param epromData
     * @param simulationName
     */
    public SimulationInfo(byte[] epromData, String simulationName) {
      this.epromData = epromData;
      this.simulationName = simulationName;
    }
  }

  /**
   * Argument tag for debug mode.
   */
  public static final String DEBUG_TAG = "-d";

  /**
   * Argument tag for window scale.
   */
  public static final String SCALE_TAG = "-s";

  /**
   * Argument tag for EPROM data path.
   */
  public static final String EPROM_TAG = "-e";

  /**
   * Should debug mode be enabled?
   */
  public boolean debugMode;

  /**
   * Scale of video window.
   */
  public int windowScale = 2;

  /**
   * EPROM data path.
   */
  public String epromPath = "data/eprom";

  /**
   * List of simulation info objectss for all found simulation configurations.
   */
  public List<SimulationInfo> simulationInfos = new ArrayList<>();

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
   * Builds environment from program arguments.
   *
   * @param args program arguments
   * @throws java.io.IOException if reading or parsing fails
   */
  public MainEnvironment(String[] args) throws IOException {
    // get arguments
    debugMode = hasArgument(args, DEBUG_TAG);

    String windowScaleArg = getArgument(args, SCALE_TAG);
    if (windowScaleArg != null) {
      try {
        windowScale = Integer.parseInt(windowScaleArg);
      } catch (NumberFormatException e) {
        System.err.println("Error parsing scale argument. Using default of " + windowScale);
      }
    }

    epromPath = Objects.requireNonNullElse(getArgument(args, EPROM_TAG), epromPath);

    // load simulation EPROMs
    System.out.println(">> Loading simulation EPROM(s) from " + epromPath);

    // get simulation configuration directory
    File epromDir = new File(epromPath);
    if (!epromDir.isDirectory()) {
      throw new IOException("Given simulation EPROM path is not a directory");
    }

    // step through ELF files
    for (final File entry : epromDir.listFiles()) {
      // ignore subdirectories
      if (entry.isDirectory()) {
        continue;
      }

      // ignore non-ELF files
      if (!entry.getName().endsWith(".elf")) {
        continue;
      }

      try {
        // read the EPROM and build a simulation info from it
        byte[] epromData = ELF.readEPROM(entry.getAbsolutePath());
        SimulationInfo simulationInfo = new SimulationInfo(epromData, entry.getName());

        // append to simulation info list
        simulationInfos.add(simulationInfo);
      } catch (IOException e) {
        throw new IOException("Error loading EPROM data. " + e.getMessage(), e);
      }
    }
  }
}
