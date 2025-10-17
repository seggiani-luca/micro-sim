package microsim;

import java.io.IOException;
import java.util.Objects;
import microsim.file.*;
import org.json.*;

/**
 * Gets and represents info related to the main program flow, including arguments, configuration and
 * EPROM files. This means handling argument parsing and file reading. Also keeps references to the
 * configuration JSON files to pass along to other info constructors (mainly for simulation
 * configuration).
 */
public class MainEnvironment {

  /**
   * Argument tag for debug mode.
   */
  public static final String DEBUG_TAG = "-d";

  /**
   * Argument tag for EPROM data path.
   */
  public static final String EPROM_TAG = "-e";

  /**
   * Argument tag for interface configuration path.
   */
  public static final String UI_CONFIG_TAG = "-ci";

  /**
   * Argument tag for simulation configuration path.
   */
  public static final String SIM_CONFIG_TAG = "-cs";

  /**
   * Signals if the debug shell should be shown.
   */
  public boolean debugMode = false;

  /**
   * Path of the EPROM data file.
   */
  public String epromDataPath = "data/eprom.elf";

  /**
   * Path of the interface configuration file.
   */
  public String iConfigPath = "conf/interface.json";

  /**
   * Path of the simulation configuration file.
   */
  public String sConfigPath = "conf/simulation.json";

  /**
   * Byte array containing EPROM data.
   */
  public byte[] epromData;

  /**
   * Should window interface be shown?
   */
  public boolean headless = false;

  /**
   * Window interface windowScale.
   */
  public int windowScale = 1;

  /**
   * Keyboard input source types.
   */
  public enum KeyboardSourceType {
    window,
    detached
  }

  /**
   * Keyboard input source.
   */
  public KeyboardSourceType keyboardSourceType;

  /**
   * The interface configuration JSON.
   */
  public JSONObject iConfig;

  /**
   * The simulation configuration JSON.
   */
  public JSONObject sConfig;

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
    debugMode = hasArgument(args, DEBUG_TAG);
    epromDataPath = Objects.requireNonNullElse(getArgument(args, EPROM_TAG), epromDataPath);
    iConfigPath = Objects.requireNonNullElse(getArgument(args, UI_CONFIG_TAG), iConfigPath);
    sConfigPath = Objects.requireNonNullElse(getArgument(args, SIM_CONFIG_TAG), sConfigPath);

    // load epromData
    epromData = ELF.readEPROM(epromDataPath);

    // load configs
    iConfig = JSON.readJSON(iConfigPath);
    sConfig = JSON.readJSON(sConfigPath);

    // get data of window interface
    JSONObject windowConfig = iConfig.getJSONObject("window");
    headless = windowConfig.optBoolean("headless", headless);
    windowScale = windowConfig.optInt("scale", windowScale);

    // get data of keyboard interface
    JSONObject keyboardConfig = iConfig.getJSONObject("keyboard");
    String keyboardSourceString = keyboardConfig.getString("source");
    switch (keyboardSourceString) {
      case "window" ->
        keyboardSourceType = KeyboardSourceType.window;
      case "detached" ->
        keyboardSourceType = KeyboardSourceType.detached;
      default ->
        throw new IOException("Unknown keyboard source in keyboard interface configuration");
    }
  }
}
