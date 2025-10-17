package microsim.ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Deque;
import microsim.Main;
import microsim.simulation.Simulation;
import microsim.simulation.component.device.video.VideoDevice;
import microsim.simulation.component.memory.MemorySpace;
import microsim.simulation.component.processor.MicroOp;
import microsim.simulation.component.processor.Processor;
import microsim.simulation.event.AttachEvent;
import microsim.simulation.event.CycleEvent;
import microsim.simulation.event.SimulationEvent;
import microsim.simulation.event.SimulationListener;

/**
 * Handles the shell shown in debug mode. Debug information is shown in 2 ways:
 * <ol>
 * <li>Via the {@link #log(java.lang.String)} method, which prints event information mid-cycle.</li>
 * <li>Via the {@link #shell()} method, which offers interactive debugging at the beginning of
 * simulation cycles. This is automatically called on {@link simulation.event.CycleEvent}
 * events.</li>
 * </ol>
 */
public class DebugShell implements SimulationListener {

  /**
   * Flag that signals whether the shell is active.
   */
  public static boolean active = false;

  /**
   * Activates debug shell.
   */
  public void activate() {
    active = true;
  }

  /**
   * Deactivates debug shell.
   */
  public void deactivate() {
    active = false;
  }

  /**
   * Flag that signals whether the shell should greet the user.
   */
  private boolean shouldGreet = true;

  /**
   * Next cycle to show shell at. Used with {@link #shouldShowShell} to jump to specific cycle.
   */
  private int nextShellCycle = 0;

  /**
   * Signals whether shell should be shown or not. Used with {@link #nextShellCycle} to jump to
   * specific cycle.
   */
  private boolean shouldShowShell = true;

  /**
   * Simulation instance to debug. Used to access processor and memory information from shell.
   */
  private Simulation simulationInstance = null;

  /**
   * Attaches a new simulation instance to the debug shell.
   *
   * @param simulationInstance simulation instance to attach
   */
  public void attachSimulation(Simulation simulationInstance) {
    this.simulationInstance = simulationInstance;
    simulationInstance.addListener(this);
  }

  /**
   * Detaches attached simulation instance. If no simulation instance is attached, nothing changes.
   */
  public void detachSimulation() {
    if (simulationInstance == null) {
      return;
    }

    simulationInstance.removeListener(this);
    this.simulationInstance = null;
  }

  /**
   * Helper to convert an int to a hexadecimal string. Used by this class and as an utility to build
   * {@link simulation.event.DebugEvent} messages.
   *
   * @param val int to convert
   * @return hexadecimal hex string representing int
   */
  public static String int32ToString(int val) {
    return String.format("0x%08x", val);
  }

  /**
   * Prints EPROM data array word by word. Used as an utility by whoever gets data from
   * {@link elf.Elf}.
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
   * Fetches processor registers from processor in current simulation and prints them.
   */
  private void printProcessorRegisters() {
    Processor proc = simulationInstance.proc;

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
    for (int i = 0; i < proc.REGISTERS - 1; i++) {
      System.out.println("\t" + mnemonics[i] + ":\t" + int32ToString(registers[i]));
    }
  }

  /**
   * Fetches MicroOp queue from processor in current simulation and prints it.
   */
  private void printProcessorMicroOps() {
    Processor proc = simulationInstance.proc;
    Deque<MicroOp> ops = proc.getMicroOps();

    if (ops.isEmpty()) {
      System.out.println("\tPipeline empty");
    }

    // loop through microops and print
    for (MicroOp op : ops) {
      int inst = op.getInstruction();
      System.out.println("\t" + op.toString());
    }
  }

  /**
   * Reads and prints data from memory at given address. Checks valid addresses and out of bound
   * reads.
   *
   * @param addr the address to read from
   */
  private void readMemoryAtAddress(String addr) {
    MemorySpace memory = simulationInstance.memory;
    int numAddr = 0;

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
   * Writes data to memory at given address. Checks valid addresses, valid data and out of bound
   * writes.
   *
   * @param addr the address to read from
   */
  private void writeMemoryAtAddress(String addr, String data) {
    MemorySpace memory = simulationInstance.memory;
    int numAddr = 0;

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

    byte numData = 0;

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
   * Receives a simulation event. {@link simulation.event.SimulationEvent} events that return a
   * non-null debug string are logged, and {@link simulation.event.CycleEvent} events launch the
   * interactive shell.
   *
   * @param e simulation event
   */
  @Override
  public void onSimulationEvent(SimulationEvent e) {
    if (!active) {
      if (e instanceof AttachEvent) {
        activate();
      } else {
        return;
      }
    }

    // if we get here we are active, print message
    String message = e.getDebugMessage();

    // is there a debug message event?
    if (message != null) {
      log(message);
    }

    // if it's a cycle, launch shell
    if (e instanceof CycleEvent cycleEvent) {
      if (shouldShowShell) {
        shell();
      } else {
        if (cycleEvent.cycle >= nextShellCycle) {
          shouldShowShell = true;
          shell();
        }
      }
    }
  }

  /**
   * Logs a debug message.
   *
   * @param message message to display
   */
  private void log(String message) {
    message = message.replace("\n", "\n\t");
    System.out.println("\t" + message);
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
    MEM
  }

  /**
   * Shows help info.
   */
  private void help(HelpPage page) {
    switch (page) {
      case GENERAL -> {
        System.out.println("Available commands:");
        System.out.println("\tstep: steps execution by 1 cycle or to specific cycle");
        System.out.println("\tcontinue: resume normal execution");
        System.out.println("\tquit: halts executions and quits");
        System.out.println("\tproc: offers processor information");
        System.out.println("\tmem: offers memory information");
        System.out.println("\trender: forces screen rendering");
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
      default ->
        throw new RuntimeException("Unkown help page");
    }
  }

  /**
   * Displays a debug shell for the current simulation instance. The shell offers the following
   * commands:
   * <ul>
   * <li>step: steps execution by 1 cycle. Operand can be specified to specify at which step to
   * stop.</li>
   * <li>continue: resume normal execution</li>
   * <li>quit: halts execution and quits.</li>
   * <li>
   * proc: offers processor information with following options:
   * <ul>
   * <li>registers: prints all registers:</li>
   * <li>pipeline: prints pipeline information:</li>
   * </ul>
   * </li>
   * <li>
   * mem: offers memory information with following options:
   * <ul>
   * <li>read: reads memory at address.</li>
   * <li>write: writes memory at address.</li>
   * </ul>
   * </li>
   * <li>
   * render: forces screen rendering
   * </li>
   * </ul>
   */
  private void shell() {
    // enter debug shell loop
    while (true) {
      if (shouldGreet) {
        greet();
      }

      System.out.print("debug> ");
      String cmd = new java.util.Scanner(System.in).nextLine().strip();

      // print help on empty commands
      if (cmd.isEmpty()) {
        help(HelpPage.GENERAL);
        continue;
      }

      String[] tokens = cmd.split("\\s+");

      switch (tokens[0]) {
        case "s":
        case "step":
          if (tokens.length == 2) {
            try {
              nextShellCycle = Integer.parseInt(tokens[1]);
              shouldShowShell = false;
            } catch (NumberFormatException e) {
              System.out.println("Invalid cycle index");
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
          if (tokens.length < 2) {
            help(HelpPage.PROC);
            continue;
          }

          switch (tokens[1]) {
            case "r":
            case "registers":
              printProcessorRegisters();
              continue;

            case "p":
            case "pipeline":
              printProcessorMicroOps();
              continue;

            default:
              System.out.println("Unknown proc option: " + cmd);
              continue;
          }
        }
        case "m":
        case "mem": {
          // print help on empty options
          if (tokens.length < 2) {
            help(HelpPage.MEM);
            continue;
          }

          switch (tokens[1]) {
            case "r":
            case "read": {
              if (tokens.length < 3) {
                System.out.println("Please specify memory address");
              }

              String addr = tokens[2];

              readMemoryAtAddress(addr);
              continue;
            }

            case "w":
            case "write": {
              if (tokens.length < 4) {
                System.out.println("Please specify memory address and data to be written");
              }

              String addr = tokens[2];
              String data = tokens[3];

              writeMemoryAtAddress(addr, data);
              continue;
            }

            default:
              System.out.println("Unknown mem option: " + cmd);
              continue;
          }
        }

        case "r":
        case "render": {
          VideoDevice videoDevice = simulationInstance.getDevice(VideoDevice.class);
          if (videoDevice == null) {
            System.out.println("Simulation doesn't have a video device mounted");
          } else {
            videoDevice.render();
          }
          continue;
        }

        default:
          System.out.println("Unknown command " + cmd);
      }
    }
  }
}
