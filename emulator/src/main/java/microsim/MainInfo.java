package microsim;

import java.io.IOException;
import java.util.Objects;
import microsim.file.*;
import org.json.*;

/**
 * Represents info related to the main program flow, including arguments, configuration and EPROM
 * files.
 */
public class MainInfo {

  public static final String DEBUG_TAG = "-d";
  public static final String EPROM_TAG = "-e";
  public static final String UI_CONFIG_TAG = "-ci";
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
   * The interface configuration JSON.
   */
  public JSONObject iConfig;

  /**
   * The simulation configuration JSON.
   */
  public JSONObject sConfig;

  /**
   * Window interface windowScale.
   */
  public int windowScale = 1;

  /**
   * Keyboard input source types.
   */
  public enum KeyboardSource {
    WINDOW
  }

  /**
   * Keyboard input source.
   */
  public KeyboardSource keyboardSource;

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
  public MainInfo(String[] args) throws IOException {
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
    windowScale = windowConfig.optInt("scale", windowScale);

    // get data of keyboard interface
    JSONObject keyboardConfig = iConfig.getJSONObject("keyboard");
    String keyboardSourceString = keyboardConfig.getString("source");
    switch (keyboardSourceString) {
      case "window" ->
        keyboardSource = KeyboardSource.WINDOW;
      default ->
        throw new IOException("Unknown keyboard source in keyboard interface configuration");
    }
  }
}
