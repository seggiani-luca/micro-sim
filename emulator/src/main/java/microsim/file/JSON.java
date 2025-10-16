package microsim.file;

import java.io.IOException;
import java.nio.file.*;
import org.json.*;

/**
 * Uses the java json library to parse a .json.
 */
public class JSON {

  /**
   * Takes a path string and returns the corresponding JSON object.
   *
   * @param path path of .json file
   * @return JSON object
   * @throws IOException if fails to open or parse .json file
   */
  public static JSONObject readJSON(String path) throws IOException {
    String fileString = Files.readString(Paths.get(path));

    JSONObject json;
    try {
      json = new JSONObject(fileString);
    } catch (JSONException e) {
      throw new IOException("Coudln't parse JSON. " + e.getMessage());
    }

    return json;
  }
}
