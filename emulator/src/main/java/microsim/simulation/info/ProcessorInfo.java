package microsim.simulation.info;

import static microsim.simulation.info.SimulationInfo.*;
import org.json.*;

/**
 * Represents info about the processor.
 */
public class ProcessorInfo {

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
