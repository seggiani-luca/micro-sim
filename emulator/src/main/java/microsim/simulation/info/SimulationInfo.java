package microsim.simulation.info;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.*;

/**
 * Represents info related to a simulation instance, including EPROM data device and component
 * configuration.
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
   * Builds simulation info from the EPROM data array and a JSON configuration file.
   *
   * @param eprom EPROM data array
   * @param config JSON configuration file
   */
  public SimulationInfo(byte[] eprom, JSONObject config) throws IOException {
    processorInfo = new ProcessorInfo(config.getJSONObject("processor"));
    memoryInfo = new MemoryInfo(config.getJSONObject("memory"), eprom);

    JSONArray devices = config.getJSONArray("devices");
    for (int i = 0; i < devices.length(); i++) {
      JSONObject device = devices.getJSONObject(i);
      String deviceType = device.getString("type");

      switch (deviceType) {
        case "video" -> {
          VideoInfo videoInfo = new VideoInfo(device);
          devicesInfo.add(videoInfo);
        }
        case "keyboard" -> {
          KeyboardInfo keyboardInfo = new KeyboardInfo(device);
          devicesInfo.add(keyboardInfo);
        }
        case "timer" -> {
          TimerInfo timerInfo = new TimerInfo(device);
          devicesInfo.add(timerInfo);
        }
        default -> {
          throw new IOException("Unknown device type " + deviceType);
        }
      }
    }
  }
}
