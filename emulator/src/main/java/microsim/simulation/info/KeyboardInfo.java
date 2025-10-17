package microsim.simulation.info;

import org.json.*;

/**
 * Represents info about the keyboard device.
 */
public class KeyboardInfo extends DeviceInfo {

  /**
   * Builds keyboard device info from the corresponding section in the JSON file.
   *
   * @param config section in JSON file
   */
  public KeyboardInfo(JSONObject config) {
    super(config);
  }
}
