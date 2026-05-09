package microsim;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import microsim.file.ELF;
import microsim.file.IMG;

/**
 * Gets and represents info related to the main program flow, including arguments and simulation
 * configuration files. Uses these to build a list of simulation info objects to later instantiate.
 */
public class MainEnvironment {

  /**
   * Represents information about a simulation instance.
   */
  public static class SimulationInfo {

    /**
     * EPROM the instance should load.
     */
    public byte[] epromData;

    /**
     * Disk image the instance should load.
     */
    public byte[] diskImage;

    /*
    * Name of simulation.
     */
    public String simulationName;

    /**
     * Constructs simulation info from EPROM data, disk image and name.
     *
     * @param epromData EPROM data of simulation
     * @param diskImage disk image of simulation
     * @param simulationName simulation name
     */
    public SimulationInfo(byte[] epromData, byte[] diskImage, String simulationName) {
      this.epromData = epromData;
      this.diskImage = diskImage;
      this.simulationName = simulationName;
    }
  }

  /**
   * Argument tag for debug mode.
   */
  public static final String DEBUG_TAG = "-d";

  /**
   * Argument tag for window scale.
   */
  public static final String SCALE_TAG = "-s";

  /**
   * Argument tag for EPROM data path.
   */
  public static final String EPROM_TAG = "-e";

  /**
   * Argument tag for disk data path.
   */
  public static final String DISK_TAG = "-i";

  /**
   * Should debug mode be enabled?
   */
  public boolean debugMode;

  /**
   * Scale of video window.
   */
  public int windowScale = 2;

  /**
   * EPROM data path.
   */
  public Path epromPath = Path.of("data/eprom");

  /**
   * Disk data path.
   */
  public Path diskPath = Path.of("data/disk");

  /**
   * List of simulation info objectss for all found simulation configurations.
   */
  public List<SimulationInfo> simulationInfos = new ArrayList<>();

  /**
   * Gets argument parameter following argument tag. With tag = "-t", from "-t arg" returns "arg".
   * If no argument is found, returns null.
   *
   * @param args program's argument string Array
   * @param tag the argument tag to search for (such as "-d")
   * @return the argument parameter as a string
   */
  private static String getArgument(String[] args, String tag) {
    // step through arguments until tag is found
    for (int i = 0; i < args.length; i++) {
      if (tag.equals(args[i])) {
        // is the next argument in bounds?
        if (i + 1 >= args.length) {
          return null;
        } else {
          // return argument
          return args[i + 1];
        }
      }
    }

    return null;
  }

  /**
   * Gets if argument tag is present (used for Boolean arguments). With tag = "-t", from "-t"
   * returns true. If no argument is found, returns false.
   *
   * @param args program's argument string Array
   * @param tag the argument tag to search for (such as "-d")
   * @return a Boolean that represents if the tag was found
   */
  private static boolean hasArgument(String[] args, String tag) {
    // step through arguments until tag is found
    for (String arg : args) {
      if (tag.equals(arg)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Gets numerical argument tag is present, otherwise returns a default. With tag = "-t 50", from
   * "-t" returns 50. With no argument, returns a default.
   *
   * @param args program's argument string Array
   * @param tag the argument tag to search for (such as "-d")
   * @param def default value of argument
   * @return the numerical argument
   */
  private static int numArgument(String[] args, String tag, int def) {
    // get argument as string
    String arg = getArgument(args, tag);

    // try converting to int
    if (arg != null) {
      try {
        return Integer.parseInt(arg);
      } catch (NumberFormatException e) {
        System.err.println("Error parsing numerical argument. Using default of " + def);
        return def;
      }
    }

    return def;
  }

  /**
   * Loads EPROMs and corresponding disks if found, building the list of simulation infos.
   */
  private void getInfos() throws IOException {
    // get EPROM data directory from path
    if (!Files.isDirectory(epromPath)) {
      throw new IOException("Given simulation EPROM path is not a directory");
    }

    // get disk image directory from path
    if (!Files.isDirectory(diskPath)) {
      throw new IOException("Given simulation disk image path is not a directory");
    }

    // step through ELF files
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(epromPath, "*.elf")) {
      for (Path entry : entries) {
        if (!Files.isRegularFile(entry)) {
          continue;
        }

        // get simulation name
        String name = entry.getFileName().toString().replaceFirst("\\.elf$", "");

        // read the EPROM
        byte[] epromData = ELF.readEPROM(entry);

        // get disk (if it exists)
        byte[] diskImage = IMG.readIMG(name, diskPath);

        // instantiate simulation info and append to info list
        SimulationInfo simulationInfo = new SimulationInfo(epromData, diskImage, name);
        simulationInfos.add(simulationInfo);
      }
    }
  }

  /**
   * Builds environment from program arguments.
   *
   * @param args program arguments
   * @throws java.io.IOException if reading or parsing fails
   */
  public MainEnvironment(String[] args) throws IOException {
    // get arguments
    debugMode = hasArgument(args, DEBUG_TAG);
    windowScale = numArgument(args, SCALE_TAG, windowScale);
    epromPath = hasArgument(args, EPROM_TAG) ? Path.of(getArgument(args, EPROM_TAG)) : epromPath;
    diskPath = hasArgument(args, DISK_TAG) ? Path.of(getArgument(args, DISK_TAG)) : diskPath;

    // load simulation EPROMs
    System.out.println(">> Loading simulation EPROM(s) from " + epromPath);
    System.out.println(">> Loading simulation disk image(s) from " + diskPath);

    // get simulation infos
    getInfos();
  }
}
