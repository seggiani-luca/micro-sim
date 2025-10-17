package microsim.simulation.info;

import static microsim.simulation.info.SimulationInfo.*;
import org.json.*;

/**
 * Represents info about any device.
 */
public abstract class DeviceInfo {

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
