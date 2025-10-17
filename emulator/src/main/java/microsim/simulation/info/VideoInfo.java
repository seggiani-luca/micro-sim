package microsim.simulation.info;

import static microsim.simulation.info.SimulationInfo.*;
import org.json.*;

/**
 * Represents info about the video device.
 */
public class VideoInfo extends DeviceInfo {

  /**
   * Frequency of video updates
   */
  public long frameFreq = 25;

  /**
   * Number of columns in text mode.
   */
  public int cols = 80;

  /**
   * Number of rows in text mode.
   */
  public int rows = 30;

  /**
   * Width of character in text mode.
   */
  public int charWidth = 8;

  /**
   * Height of character in text mode.
   */
  public int charHeight = 16;

  /**
   * Path of character set image.
   */
  public String charsetPath;

  /**
   * Width (in characters) of the character set image. Height is not needed as we are targeting 256
   * character extended ASCII.
   */
  public int charsetSize = 16;

  /**
   * Builds video device info from the corresponding section in the JSON file.
   *
   * @param config section in JSON file
   */
  public VideoInfo(JSONObject config) {
    super(config);

    frameFreq = config.getInt("frame_frequency");
    cols = config.getInt("columns");
    rows = config.getInt("rows");
    charWidth = config.getInt("character_width");
    charHeight = config.getInt("character_height");
    charsetPath = config.getString("charset");
    charsetSize = config.getInt("charset_size");
  }
}
