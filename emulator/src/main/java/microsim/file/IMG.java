package microsim.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles disk images, reading them into byte arrays, and syncing them with simulations.
 */
public class IMG {

  /**
   * Takes a path string, a simulation name, and returns the corresponding disk image byte array (if
   * found).
   *
   * @param name name of simulation
   * @param dir path to look for disk data in
   * @return disk data byte array
   * @throws IOException if fails to open file
   */
  public static byte[] readIMG(String name, Path dir) throws IOException {
    // look for file
    Path target = dir.resolve(name + ".img");
    if (Files.exists(target) && Files.isRegularFile(target)) {
      System.out.println("Found disk image for simulation " + name);

      // read bytes and return
      return Files.readAllBytes(target);
    }

    // file doesn't exist, assume it doesn't exist yet
    return null;
  }

  /**
   * Takes a path string, a simulation name, a byte array and stores a disk image.
   *
   * @param name name of simulation
   * @param dir path to store disk data in
   * @param bytes data to store
   * @throws java.io.IOException if fails to open or create file
   */
  public static void writeIMG(String name, Path dir, byte[] bytes) throws IOException {
    // get target path
    Path target = dir.resolve(name + ".img");

    // write disk image
    Files.write(target, bytes);
    System.out.println("Stored disk image at " + target);
  }
}
