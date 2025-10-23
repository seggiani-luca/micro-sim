package microsim;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import microsim.file.*;
import microsim.simulation.info.SimulationInfo;
import org.json.*;

/**
 * Gets and represents info related to the main program flow, including arguments and machine
 * configuration files. Uses these to build a list of simulation info objects to later instantiate.
 */
public class MainEnvironment {

  /**
   * Argument tag for debug mode.
   */
  public static final String DEBUG_TAG = "-d";

  /**
   * Argument tag for configuration directory path.
   */
  public static final String CONFIG_TAG = "-c";

  /**
   * Path of the configuration directory path.
   */
  public String configPath = "conf/";

  /**
   * List of simulation infos for all found machine configurations.
   */
  public List<SimulationInfo> simulationInfos = new LinkedList<>();

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
   * Builds a MainInfo object from program arguments.
   *
   * @param args program arguments
   * @throws java.io.IOException if reading or parsing fails
   */
  public MainEnvironment(String[] args) throws IOException {
    // get arguments
    boolean debugMode = hasArgument(args, DEBUG_TAG);
    configPath = Objects.requireNonNullElse(getArgument(args, CONFIG_TAG), configPath);

    // load machine configurations
    System.out.println("Loading machine configurations from " + configPath);

    // get machine configuration directory
    File configDir = new File(configPath);
    if (!configDir.isDirectory()) {
      throw new IOException("Given machine configuration path is not a directory");
    }

    // step through machine configurations
    for (final File entry : configDir.listFiles()) {
      // ignore subdirectories
      if (!entry.isDirectory()) {
        // read the JSON and build a simulation info from it
        JSONObject config = JSON.readJSON(entry.getPath());
        SimulationInfo simulationInfo = new SimulationInfo(config);

        // set debug mode if needed
        if (debugMode) {
          simulationInfo.debugMode = true;
        }

        // append to simulation info list
        simulationInfos.add(simulationInfo);
      }
    }
  }
}
