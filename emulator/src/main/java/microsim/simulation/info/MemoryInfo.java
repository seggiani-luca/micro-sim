package microsim.simulation.info;

import static microsim.simulation.info.SimulationInfo.*;
import org.json.*;

/**
 * Represents info about the memory space.
 */
public class MemoryInfo {

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
