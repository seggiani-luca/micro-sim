package microsim.simulation.info;

import org.json.*;

/**
 * Represents info about the timer device.
 */
public class TimerInfo extends DeviceInfo {

  /**
   * Frequency of timer clock.
   */
  public int masterFreq;

  /**
   * Builds timer device info from the corresponding section in the JSON file.
   *
   * @param config section in JSON file
   */
  public TimerInfo(JSONObject config) {
    super(config);

    masterFreq = config.getInt("master_frequency");
  }
}
