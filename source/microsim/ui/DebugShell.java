package microsim.ui;

import microsim.simulation.*;
import microsim.simulation.component.*;

/**
 * Handles the shell shown in debug mode. Debug information is shown in 2 ways:
 * <ol>
 * <li>
 * Via the {@link #log(java.lang.String)} method, which prints information mid-cycle.
 * </li>
 * <li>
 * Via the {@link #shell(microsim.simulation.Simulation)} method, which offers interactive debugging
 * at the end of cycles.
 * </li>
 * </ol>
 */
public class DebugShell {

  /**
   * Simulation instance to debug.
   */
  public static Simulation simulationInstance = null;

  /**
   * Fetches processor registers from processor in current simulation and displays them.
   */
  private static void printProcessorRegisters() {
    Processor proc = simulationInstance.proc;

    char[] registers = proc.getRegisters();

    System.out.println("\t%a:\t" + String.format("%04X", registers[0] & 0xffff));
    System.out.println("\t%b:\t" + String.format("%04X", registers[1] & 0xffff));
    System.out.println("\t%c:\t" + String.format("%04X", registers[2] & 0xffff));
    System.out.println("\t%d:\t" + String.format("%04X", registers[3] & 0xffff));
    System.out.println("\t%ip:\t" + String.format("%04X", registers[4] & 0xffff));
    System.out.println("\t%sp:\t" + String.format("%04X", registers[5] & 0xffff));

    boolean[] flags = proc.getFlagRegister();

    System.out.print("\tflag:\t{");
    System.out.print("of: " + (flags[0] ? 1 : 0));
    System.out.print(" sf: " + (flags[1] ? 1 : 0));
    System.out.print(" zf: " + (flags[2] ? 1 : 0));
    System.out.print(" cf: " + (flags[3] ? 1 : 0));

    System.out.println("}");
  }

  /**
   * Fetches processor state from processor in current simulation and displays it.
   */
  private static void printProcessorState() {
    Processor proc = simulationInstance.proc;
    Processor.ProcessorState state = proc.getState();

    System.out.println("\tPresent state is: " + state.name());
  }

  /**
   * Reads and prints data from memory at given address
   *
   * @param addr the address to read from
   */
  private static void printMemoryAtAddress(String addr) {
    MemorySpace memory = simulationInstance.memory;
    // TODO: implement
  }

  /**
   * Logs a simulation event. TODO: turn this into an event listener
   *
   * @param message message of the event
   */
  public static void log(String message) {
    // for now check if there is a debugInstance
    if (simulationInstance == null) {
      return;
    }

    // tab message
    message = message.replace("\n", "\n\t");
    System.out.println("\t" + message);
  }

  /**
   * Displays a debug shell for the current simulation instance. The shell offers the following
   * commands:
   * <ul>
   * <li>
   * step: steps execution by 1 cycle.
   * </li>
   * <li>
   * quit: halts execution and quits.
   * </li>
   * <li>
   * proc: offers processor information with following options:
   * <ul>
   * <li>
   * registers: prints all registers-
   * </li>
   * <li>
   * state prints state information-
   * </li>
   * </ul>
   * </li>
   * <li>
   * mem: offers memory information with following options:
   * <ul>
   * <li>
   * read: reads memory at address.
   * </li>
   * </ul>
   * </li>
   * </ul>
   *
   * @param which the simulation instance that called the shell
   */
  public static void shell(Simulation which) {
    // only debug if it's current debugInstance
    if (simulationInstance != which) {
      return;
    }

    // enter debug shell loop
    while (true) {
      System.out.print("debug> ");
      String cmd = new java.util.Scanner(System.in).nextLine().strip();

      // print help on empty commands
      if (cmd.isEmpty()) {
        System.out.println("Available commands:");
        System.out.println("\tstep: steps execution by 1 cycle");
        System.out.println("\tquit: halts executions and quits");
        System.out.println("\tproc: offers processor information");
        System.out.println("\tmem: offers memory information");
        continue;
      }

      String[] tokens = cmd.split("\\s+");

      switch (tokens[0]) {
        case "s":
        case "step":
          return;

        case "q":
        case "quit":
          System.exit(0);

        case "p":
        case "proc": {
          // print help on empty options
          if (tokens.length < 2) {
            System.out.println("Available proc options:");
            System.out.println("\tregisters: prints all registers");
            System.out.println("\tstate: prints state information");
            continue;
          }

          switch (tokens[1]) {
            case "r":
            case "registers":
              printProcessorRegisters();
              continue;

            case "s":
            case "state":
              printProcessorState();
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
            System.out.println("Available mem options:");
            System.out.println("\tread: reads memory at address");
            continue;
          }

          switch (tokens[1]) {
            case "r":
            case "read":
              if (tokens.length < 3) {
                System.out.println("Please specify memory address");
              }

              String addr = tokens[2];
              printMemoryAtAddress(addr);

            default:
              System.out.println("Unknown mem option: " + cmd);
              continue;
          }
        }

        default:
          System.out.println("Unknown command " + cmd);
          continue;
      }
    }
  }
}
