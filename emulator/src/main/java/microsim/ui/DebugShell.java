package microsim.ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import microsim.Main;
import microsim.simulation.*;
import microsim.simulation.component.device.video.*;
import microsim.simulation.component.memory.*;
import microsim.simulation.component.processor.*;
import microsim.simulation.event.*;

/**
 * Handles the shell shown in debug mode.
 */
public class DebugShell implements SimulationListener {

  /**
   * Static flag that signals whether debugging is enabled. This is static as it's used by
   * components to decide whether to raise debug events or not (raising them anyways is
   * inefficient).
   */
  private static volatile boolean debuggingEnabled = false;

  /**
   * Returns whether debugging is enabled.
   *
   * @return is debugging enabled?
   */
  public static boolean isDebuggingEnabled() {
    return debuggingEnabled;
  }

  /**
   * Activates debug shell.
   */
  public synchronized void activate() {
    debuggingEnabled = true;
  }

  /**
   * Deactivates debug shell.
   */
  public synchronized void deactivate() {
    debuggingEnabled = false;

    // clear event queues
    for (Queue<SimulationEvent> eventQueue : eventQueues) {
      eventQueue.clear();
    }
  }

  /**
   * Static flag that signals whether the world is stopped. This is static as the first debug shell
   * instance that stops the world stops it for all simulation instances.
   */
  private static volatile boolean isWorldStopped = false;

  private static void stopTheWorld() {
    isWorldStopped = true;

    // sleep to sync
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      throw new RuntimeException("Debug shell was interrupted while waiting for threads");
    }
  }

  private static void startTheWorld() {
    isWorldStopped = false;
  }

  /**
   * Returns whether world is stopped. {@link microsim.simulation.component.device.ThreadedIoDevice}
   * devices are meant to check this, via the
   * {@link microsim.simulation.component.device.ThreadedIoDevice#smartSpin(long)} method, to
   * correctly stop when asked to.
   *
   * @return is world stopped?
   */
  public static boolean isWorldStopped() {
    return isWorldStopped;
  }

  /**
   * Cycle counter for multi-cycle stepping.
   */
  private long remainingCycles = -1;

  /**
   * Keeps track of which instance we are multi-cycle stepping on.
   */
  private long cycleInstance;

  /**
   * Flag that signals whether the shell should greet the user. Set to false at first greet to have
   * it greet on startup.
   */
  private static volatile boolean shouldGreet = true;

  /**
   * Simulation instances to debug. Used to access processor and memory information from shell.
   */
  private final List<Simulation> simulationInstances = new ArrayList<>();

  private final List<Queue<SimulationEvent>> eventQueues = new ArrayList<>();

  /**
   * Attaches a new simulation instance to the debug shell (basically makes self a listener and
   * takes a reference to show on demand information from shell).
   *
   * @param simulationInstance simulation instance to attach
   */
  public void attachSimulation(Simulation simulationInstance) {
    simulationInstances.add(simulationInstance);
    simulationInstance.addListener(this);

    Queue<SimulationEvent> eventQueue = new LinkedList<>();
    eventQueues.add(eventQueue);
  }

  /**
   * Helper to convert an int to a hexadecimal string. Used by this class and as an utility to build
   * {@link simulation.event.DebugEvent} messages. This method alone is fine for most messages as
   * all rv32i registers are 32 bit.
   *
   * @param val int to convert
   * @return hexadecimal hex string representing int
   */
  public static String int32ToString(int val) {
    return String.format("0x%08x", val);
  }

  /**
   * Prints EPROM data array word by word. Used as an utility by whoever gets data from
   * {@link file.ELF}.
   *
   * @param eprom EPROM data array
   */
  public static void printEPROM(byte[] eprom) {
    System.out.println("Read EPROM data:");

    // wrap in ByteBuffer
    ByteBuffer epromBuffer = ByteBuffer.wrap(eprom).order(ByteOrder.LITTLE_ENDIAN);

    // print as many words as possible
    while (epromBuffer.remaining() >= 4) {
      System.out.println("\t" + DebugShell.int32ToString(epromBuffer.position()) + ": "
              + DebugShell.int32ToString(epromBuffer.getInt()));
    }

    // print last word
    int lastPos = epromBuffer.position();

    int last = 0, shift = 0;
    while (epromBuffer.remaining() >= 1) {
      last |= (epromBuffer.get() & 0xff) << shift;
      shift += 8;
    }

    System.out.println("\t" + DebugShell.int32ToString(lastPos) + ": "
            + DebugShell.int32ToString(last) + "\n");
  }

  /**
   * Fetches processor registers from processor in simulation at index and prints them.
   *
   * @param idx index of simulation
   */
  private void printProcessorRegisters(int idx) {
    Processor proc = simulationInstances.get(idx).proc;

    int[] registers = proc.getRegisters();
    int pc = proc.getPc();

    System.out.println("\tpc:\t" + int32ToString(pc));
    System.out.println("\tzero:\t0"); // defaults to zero register

    final String[] mnemonics = {
      "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0/fp", "s1", "a0", "a1", "a2", "a3", "a4", "a5",
      "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t3", "t4", "t5",
      "t6"
    };

    // loop through registers and print
    for (int i = 0; i < Processor.REGISTERS - 1; i++) {
      System.out.println("\t" + mnemonics[i] + ":\t" + int32ToString(registers[i]));
    }
  }

  /**
   * Fetches MicroOp queue from processor in simulation at index and prints it.
   *
   * @param idx index of simulation
   */
  private void printProcessorMicroOps(int idx) {
    Processor proc = simulationInstances.get(idx).proc;
    Deque<MicroOp> ops = proc.getMicroOps();

    if (ops.isEmpty()) {
      System.out.println("\tPipeline empty");
    }

    // loop through microops and print
    for (MicroOp op : ops) {
      System.out.println("\t" + op.toString());
    }
  }

  /**
   * Prints all active instances, including index mappings and power state.
   */
  private void printInstances() {
    for (int i = 0; i < simulationInstances.size(); i++) {
      Simulation instance = simulationInstances.get(i);
      System.out.print("\t" + i + ":\t\"" + instance.name + "\"\t");
      if (instance.isRunning()) {
        System.out.println("(powered on)");
      } else {
        System.out.println("(powered off)");
      }
    }
  }

  /**
   * Reads and prints data from memory at given address from simulation at index. Checks valid
   * addresses and out of bound reads.
   *
   * @param idx index of simulation
   * @param addr the address to read from
   */
  private void readMemoryAtAddress(int idx, String addr) {
    MemorySpace memory = simulationInstances.get(idx).memory;
    int numAddr;

    // is address valid?
    try {
      numAddr = (int) Integer.parseInt(addr, 16);
    } catch (NumberFormatException e) {
      System.err.println("Invalid address. " + e.getMessage());
      return;
    }

    // is addres in bounds?
    if (!memory.inBounds(numAddr)) {
      System.err.println("Address out of bounds");
      return;
    }

    // read
    byte data = memory.readMemory(numAddr, true);

    System.out.println("Read " + String.format("%02X", data & 0xff)
            + " from memory at address " + int32ToString(numAddr));
  }

  /**
   * Writes data to memory at given address from simulation at index. Checks valid addresses, valid
   * data and out of bound writes.
   *
   * @param idx index of simulation
   * @param addr the address to read from
   */
  private void writeMemoryAtAddress(int idx, String addr, String data) {
    MemorySpace memory = simulationInstances.get(idx).memory;
    int numAddr;

    // is address valid?
    try {
      numAddr = (int) Integer.parseInt(addr, 16);
    } catch (NumberFormatException e) {
      System.err.println("Invalid address. " + e.getMessage());
      return;
    }

    // is addres in bounds?
    if (!memory.inBounds(numAddr)) {
      System.err.println("Address out of bounds");
      return;
    }

    byte numData;

    // is data valid?
    try {
      numData = (byte) Integer.parseInt(data, 16);
    } catch (NumberFormatException e) {
      System.err.println("Invalid data: " + e.getMessage());
      return;
    }

    // read
    memory.writeMemory(numAddr, numData, true);
    System.out.println("Wrote " + String.format("%02X", numData & 0xff)
            + " to memory at address " + int32ToString(numAddr));
  }

  /**
   * Receives a simulation event.
   *
   * @param e simulation event
   */
  @Override
  public synchronized void onSimulationEvent(SimulationEvent e) {
    // only respond if debugging
    if (!debuggingEnabled && !(e instanceof BreakEvent)) {
      return;
    }

    // ignore video frames
    if (e instanceof FrameEvent) {
      return;
    }

    // activate shell if requested
    if (e instanceof BreakEvent) {
      activate();
    }

    // handle multi-cycling
    if (remainingCycles != -1 && (e instanceof CycleEvent)) {
      if (simulationInstances.indexOf(e.owner.simulation) == cycleInstance) {
        remainingCycles--;
        if (remainingCycles == 0) {
          remainingCycles = -1;
        }
      }
    }

    // check for waits on powered off instances
    if (remainingCycles != -1 && (e instanceof HaltEvent)) {
      if (simulationInstances.indexOf(e.owner.simulation) == cycleInstance) {
        remainingCycles = -1;
      }
    }

    // normal event, queue it or enter shell
    int idx = simulationInstances.indexOf(e.owner.simulation);
    Queue<SimulationEvent> eventQueue = eventQueues.get(idx);

    if (remainingCycles == -1 && e instanceof CycleEvent ce) {
      // greet if needed
      if (shouldGreet) {
        greet();
      }

      // print buffered events
      while (!eventQueue.isEmpty()) {
        log(eventQueue.remove());
      }
      log(ce);

      // enter shell
      stopTheWorld();
      shell();
      startTheWorld();
    } else {
      eventQueue.add(e);
    }
  }

  /**
   * Logs a debug event.
   *
   * @param event event to log
   */
  private void log(SimulationEvent event) {
    String message = event.getDebugMessage();
    String simulation = event.owner.simulation.name;

    message = message.replace("\n", "\n\t");
    System.out.println("\t" + "\"" + simulation + "\" -> " + message);
  }

  /**
   * Greets the user and gives basic info.
   */
  private void greet() {
    System.out.println("Welcome to the micro-sim debug shell, version " + Main.VERSION);
    System.out.println("Addresses are base-16, no 0x prefix.\n");
    for (HelpPage page : HelpPage.values()) {
      help(page);
      System.out.println();
    }

    System.out.println("For more info see the documentation at docs/index.html. "
            + "If not found build with make docs.\n");

    shouldGreet = false;
  }

  /**
   * Enum to index help pages.
   */
  private enum HelpPage {
    GENERAL,
    PROC,
    MEM,
    THREAD
  }

  /**
   * Shows help info.
   */
  private void help(HelpPage page) {
    switch (page) {
      case GENERAL -> {
        System.out.println("Available commands:");
        System.out.println("\tstep: steps past 1 or more cycles");
        System.out.println("\tcontinue: resumes normal execution");
        System.out.println("\tquit: halts executions and quits");
        System.out.println("\tproc: offers processor information");
        System.out.println("\tmem: offers memory information");
        System.out.println("\trender: forces screen rendering");
        System.out.println("\tthread: controls device threads");
        System.out.println("\tinstance: shows current instances");
      }
      case PROC -> {
        System.out.println("Available proc options:");
        System.out.println("\tregisters: prints all registers");
        System.out.println("\tpipeline: prints pipeline information");
      }
      case MEM -> {
        System.out.println("Available mem options:");
        System.out.println("\tread: reads memory at address");
        System.out.println("\twrite: reads memory at address");
      }
      case THREAD -> {
        System.out.println("Available thread options:");
        System.out.println("\tstop: stops all device threads");
        System.out.println("\tresume: resumes all device threads");
      }
      default ->
        throw new RuntimeException("Unknown help page");
    }
  }

  /**
   * Helpers that gets the numerical index of a simulation from a string. Does valid format and
   * bound checking.
   *
   * @param idxString string representing simulation index
   * @return actual simulation index
   */
  private int getSimulationIndex(String idxString) {
    int idx;
    try {
      idx = Integer.parseInt(idxString);

    } catch (NumberFormatException e) {
      System.out.println("Invalid simulation index. " + e.getMessage());
      return -1;
    }

    if (idx < 0 || idx >= simulationInstances.size()) {
      System.out.println("Simulation index out of bounds.");
      return -1;
    }

    if (!simulationInstances.get(idx).isRunning()) {
      System.out.println("Rquested instance is powered off");
      return -1;
    }

    return idx;
  }

  /**
   * Displays a debug shell for the current simulation instance.
   */
  private void shell() {
    // enter debug shell loop
    while (true) {
      System.out.print("debug> ");
      String cmd = new Scanner(System.in).nextLine().strip();

      // print help on empty commands
      if (cmd.isEmpty()) {
        help(HelpPage.GENERAL);
        continue;
      }

      String[] tokens = cmd.split("\\s+");

      // parse
      switch (tokens[0]) {
        case "s":
        case "step":
          if (tokens.length == 3) {
            int gotCycleInstance = getSimulationIndex(tokens[1]);
            if (gotCycleInstance == -1) {
              continue;
            }
            cycleInstance = gotCycleInstance;

            try {
              remainingCycles = Integer.parseInt(tokens[2]);
            } catch (NumberFormatException e) {
              System.out.println("Invalid cycle amount");
              continue;
            }
          }
          return;

        case "c":
        case "continue":
          deactivate();
          return;

        case "q":
        case "quit":
          System.exit(0);

        case "p":
        case "proc": {
          // print help on empty options
          if (tokens.length < 3) {
            help(HelpPage.PROC);
            continue;
          }

          int idx = getSimulationIndex(tokens[2]);
          if (idx == -1) {
            continue;
          }

          switch (tokens[1]) {
            case "r", "registers" -> {
              printProcessorRegisters(idx);
              continue;
            }
            case "p", "pipeline" -> {
              printProcessorMicroOps(idx);
              continue;
            }

            default -> {
              System.out.println("Unknown proc option: " + cmd);
              continue;
            }
          }
        }
        case "m":
        case "mem": {
          // print help on empty options
          if (tokens.length < 3) {
            help(HelpPage.MEM);
            continue;
          }

          int idx = getSimulationIndex(tokens[2]);
          if (idx == -1) {
            continue;
          }

          switch (tokens[1]) {
            case "r", "read" -> {
              if (tokens.length < 4) {
                System.out.println("Please specify memory address");
                continue;
              }

              String addr = tokens[3];

              readMemoryAtAddress(idx, addr);
              continue;
            }
            case "w", "write" -> {
              if (tokens.length < 5) {
                System.out.println("Please specify memory address and data to be written");
                continue;
              }

              String addr = tokens[3];
              String data = tokens[4];

              writeMemoryAtAddress(idx, addr, data);
              continue;
            }

            default -> {
              System.out.println("Unknown mem option: " + cmd);
              continue;
            }
          }
        }

        case "r":
        case "render": {
          if (tokens.length < 2) {
            System.out.println("Please specify simulation index");
            continue;
          }

          int idx = getSimulationIndex(tokens[1]);
          if (idx == -1) {
            continue;
          }

          VideoDevice videoDevice = simulationInstances.get(idx).video;
          videoDevice.render();
          continue;
        }

        case "t":
        case "thread": {
          // print help on empty options
          if (tokens.length < 2) {
            help(HelpPage.THREAD);
            continue;
          }

          switch (tokens[1]) {
            case "s", "stop" -> {
              System.out.println("Stopping all device threads...");
              stopTheWorld();

              continue;
            }
            case "r", "resume" -> {
              System.out.println("Resuming all device threads...");
              startTheWorld();
              continue;
            }

            default -> {
              System.out.println("Unknown thread option: " + cmd);
              continue;
            }
          }
        }

        case "i":
        case "instance": {
          printInstances();
          continue;
        }

        default:
          System.out.println("Unknown command " + cmd);
      }
    }
  }
}
