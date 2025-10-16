package microsim.simulation.info;

import java.io.IOException;
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

  // TODO: cut these up in separate classes
  /**
   * Represents info about the processor.
   */
  public static class ProcessorInfo {

    /**
     * Reset value of program counter {@link #pc}.
     */
    public int resetInstructionAddress;

    /**
     * Builds processor info from the corresponding section in the JSON file.
     *
     * @param config section in JSON file
     */
    ProcessorInfo(JSONObject config) {
      resetInstructionAddress = optHex(config, "reset_instruction_address", 0x0000_0000);
    }
  }

  /**
   * Info about processor of this simulation.
   */
  public ProcessorInfo processorInfo;

  /**
   * Represents info about the memory space.
   */
  public static class MemoryInfo {

    /**
     * Beginning of EPROM region.
     */
    public int epromStart;

    /**
     * End of EPROM region.
     */
    public int epromEnd;
    /**
     * Beginning of RAM region.
     */
    public int ramStart;

    /**
     * End of RAM region.
     */
    public int ramEnd;

    /**
     * Beginning of VRAM region.
     */
    public int vramStart;

    /**
     * End of VRAM region.
     */
    public int vramEnd;

    /**
     * Should EPROM writes be allowed?
     */
    public boolean allowEpromWrites;

    /**
     * Should EPROM reads be allowed?
     */
    public boolean allowVramReads;

    /**
     * Byte array containing EPROM data.
     */
    public byte[] epromData;

    /**
     * Builds memory info from the corresponding section in the JSON file.
     *
     * @param config section in JSON file
     */
    MemoryInfo(JSONObject config, byte[] epromData) {
      epromStart = getHex(config, "eprom_start");
      epromEnd = getHex(config, "eprom_end");
      ramStart = getHex(config, "ram_start");
      ramEnd = getHex(config, "ram_end");
      vramStart = getHex(config, "vram_start");
      vramEnd = getHex(config, "vram_end");

      allowEpromWrites = config.optBoolean("allow_eprom_writes", false);
      allowVramReads = config.optBoolean("appow_vram_reads", true);

      this.epromData = epromData;
    }
  }

  /**
   * Info about memory space of this simulation.
   */
  public MemoryInfo memoryInfo;

  /**
   * Represents info about any device.
   */
  public abstract static class DeviceInfo {

    /**
     * Base address of device.
     */
    public int base;

    /**
     * Builds device info from the corresponding section in the JSON file.
     *
     * @param config section in JSON file
     */
    DeviceInfo(JSONObject config) {
      base = getHex(config, "base");
    }
  }

  /**
   * Represents info about the video device.
   */
  public static class VideoInfo extends DeviceInfo {

    public VideoInfo(JSONObject config) {
      super(config);
    }
  }

  /**
   * Represents info about the keyboard device.
   */
  public static class KeyboardInfo extends DeviceInfo {

    public KeyboardInfo(JSONObject config) {
      super(config);
    }
  }

  /**
   * Represents info about the timer device.
   */
  public static class TimerInfo extends DeviceInfo {

    public TimerInfo(JSONObject config) {
      super(config);
    }
  }

  /**
   * Info about devices of this simulation.
   */
  public List<DeviceInfo> devicesInfo;

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
