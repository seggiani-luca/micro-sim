package microsim.simulation.info;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import microsim.file.ELF;
import org.json.*;

/**
 * Represents info related to a simulation instance, including EPROM data, device and component
 * configuration, and user interface configuration.
 */
public class SimulationInfo {

  /**
   * Helper for hexadecimal integer parsing. Handles both "0x" prefixed and non-prefixed hexes.
   *
   * @param json JSON to grab hex from
   * @param tag tag of hex in JSON
   * @return corresponding integer
   */
  public static int getHex(JSONObject json, String tag) {
    String hex = json.getString(tag);

    hex = hex.replace("_", ""); // ignore "_" separators
    int val = Integer.parseInt(hex.startsWith("0x") ? hex.substring(2) : hex, 16);
    return val;
  }

  /**
   * Helper for optional hexadecimal integer parsing. Handles both "0x" prefixed and non-prefixed
   * hexes. A default value is to be provided if tag is not found.
   *
   * @param json JSON to grab hex from
   * @param tag tag of hex in JSON
   * @param def default value
   * @return corresponding integer
   */
  public static int optHex(JSONObject json, String tag, int def) {
    String hex = json.optString(tag);
    if (hex.isEmpty()) {
      return def;
    }

    hex = hex.replace("_", ""); // ignore "_" separators
    int val = Integer.parseInt(hex.startsWith("0x") ? hex.substring(2) : hex, 16);
    return val;
  }

  /**
   * Name of simulation instance.
   */
  public String machineName;

  /**
   * Should the debug shell be shown?
   */
  public boolean debugMode = false;

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
   * Info about processor of this simulation.
   */
  public ProcessorInfo processorInfo;

  /**
   * Info about memory space of this simulation.
   */
  public MemoryInfo memoryInfo;

  /**
   * Info about devices of this simulation.
   */
  public List<DeviceInfo> devicesInfo = new LinkedList<>();

  /**
   * Map of device info factories to parse device list.
   */
  public static final Map<String, Function<JSONObject, DeviceInfo>> DEVICE_INFO_FACTORIES = Map.of(
          "video", VideoInfo::new,
          "keyboard", KeyboardInfo::new,
          "timer", TimerInfo::new
  );

  /**
   * Builds simulation info from a JSON machine configuration file.
   *
   * @param config JSON configuration file
   * @throws IOException if JSON parsing fails
   */
  public SimulationInfo(JSONObject config) throws IOException {
    // get machine name
    machineName = config.getString("machine_name");

    // load eprom data
    String epromDataPath = config.getString("eprom_path");
    byte[] epromData = ELF.readEPROM(epromDataPath);

    // configure interfaces
    JSONObject interfaceConfig = config.getJSONObject("interface");
    configureInterfaces(interfaceConfig);

    // configure simulation
    JSONObject simulationConfig = config.getJSONObject("simulation");
    configureSimulation(simulationConfig, epromData);
  }

  /**
   * Configures interfaces based on given JSON configuration object.
   *
   * @param config JSON configuration object
   */
  private void configureInterfaces(JSONObject config) throws IOException {
    // get data of window interface
    JSONObject windowConfig = config.getJSONObject("window");

    // get window mode and scale
    headless = windowConfig.optBoolean("headless", headless);
    windowScale = windowConfig.optInt("scale", windowScale);

    // get data of keyboard interface
    JSONObject keyboardConfig = config.getJSONObject("keyboard");

    // get keyboard source type
    String keyboardSourceString = keyboardConfig.getString("source");
    keyboardSourceType = KeyboardSourceType.valueOf(keyboardSourceString.toLowerCase());
  }

  /**
   * Configures simulation based on given JSON configuration object and EPROM byte array.
   *
   * @param config JSON configuration object
   * @param epromData EPROM byte array
   */
  private void configureSimulation(JSONObject config, byte[] epromData) throws IOException {
    // get processor and memory information
    processorInfo = new ProcessorInfo(config.getJSONObject("processor"));
    memoryInfo = new MemoryInfo(config.getJSONObject("memory"), epromData);

    // get device information
    JSONArray devices = config.getJSONArray("devices");
    for (int i = 0; i < devices.length(); i++) {
      JSONObject device = devices.getJSONObject(i);
      String deviceType = device.getString("type");

      // get the correct factory, if it exists
      Function<JSONObject, DeviceInfo> factory = DEVICE_INFO_FACTORIES.get(deviceType);
      if (factory == null) {
        throw new IOException("Unknown device type while parsing JSON " + deviceType);
      }

      // use it to build device info
      DeviceInfo deviceInfo = factory.apply(device);
      devicesInfo.add(deviceInfo);
    }
  }
}
