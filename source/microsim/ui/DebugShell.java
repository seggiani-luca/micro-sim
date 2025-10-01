package microsim.ui;

import microsim.simulation.component.processor.*;
import microsim.simulation.*;
import microsim.simulation.component.*;
import microsim.simulation.event.*;

/**
 * Handles the shell shown in debug mode. Debug information is shown in 2 ways:
 * <ol>
 * <li>Via the {@link #log(java.lang.String)} method, which prints information mid-cycle.</li>
 * <li>Via the {@link #shell()} method, which offers interactive debugging at the end of
 * cycles.</li>
 * </ol>
 */
public class DebugShell implements SimulationListener {

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
   * Fetches processor registers from processor in current simulation and displays them.
   */
  private void printProcessorRegisters() {
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
  private void printProcessorState() {
    Processor proc = simulationInstance.proc;
    Processor.ProcessorState state = proc.getState();

    System.out.println("\tPresent state is: " + state.name());
  }

  /**
   * Reads and prints data from memory at given address.
   *
   * @param addr the address to read from
   */
  private void readMemoryAtAddress(String addr) {
    MemorySpace memory = simulationInstance.memory;
    char numAddr = 0;

    try {
      numAddr = (char) Integer.parseInt(addr, 16);
    } catch (NumberFormatException e) {
      System.out.println("Invalid address: " + e.getMessage());
    }

    if (numAddr < MemorySpace.RAM_BEG || numAddr >= MemorySpace.EPROM_END) {
      System.out.println("Invalid address: out of bounds");
    }

    // read
    byte data = memory.readMemory(numAddr);
    System.out.println("Read " + String.format("%02X", data & 0xff)
      + " from memory at address " + String.format("%04X", numAddr & 0xffff));
  }

  /**
   * Writes data to memory at given address.
   *
   * @param addr the address to read from
   */
  private void writeMemoryAtAddress(String addr, String data) {
    MemorySpace memory = simulationInstance.memory;
    char numAddr = 0;

    try {
      numAddr = (char) Integer.parseInt(addr, 16);
    } catch (NumberFormatException e) {
      System.out.println("Invalid address: " + e.getMessage());
    }

    if (numAddr < MemorySpace.RAM_BEG || numAddr >= MemorySpace.EPROM_END) {
      System.out.println("Invalid address: out of bounds");
    }

    byte numData = 0;

    try {
      numData = (byte) Integer.parseInt(data, 16);
    } catch (NumberFormatException e) {
      System.out.println("Invalid data: " + e.getMessage());
    }

    // read
    memory.writeMemory(numAddr, numData);
    System.out.println("Wrote " + String.format("%02X", numData & 0xff)
      + " to memory at address " + String.format("%04X", numAddr & 0xffff));
  }

  /**
   * Receives a simulation event. {@link microsim.simulation.event.SimulationEvent} events that
   * return a non-null debug string are logged, and {@link microsim.simulation.event.CycleEvent}
   * events launch the interactive shell.
   *
   * @param e simulation event
   */
  @Override
  public void onSimulationEvent(SimulationEvent e) {
    if (e instanceof SimulationEvent simulationEvent) {
      String message = simulationEvent.getDebugMessage();

      // is there a debug message event?
      if (message != null) {
        log(message);
      }

      // if it's a cycle, launch shell
      if (e instanceof CycleEvent) {
        shell();
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
   * Displays a debug shell for the current simulation instance. The shell offers the following
   * commands:
   * <ul>
   * <li>step: steps execution by 1 cycle.</li>
   * <li>quit: halts execution and quits.</li>
   * <li>
   * proc: offers processor information with following options:
   * <ul>
   * <li>registers: prints all registers:</li>
   * <li>state prints state information:</li>
   * </ul>
   * </li>
   * <li>
   * mem: offers memory information with following options:
   * <ul>
   * <li>read: reads memory at address.</li>
   * <li>write: writes memory at address.</li>
   * </ul>
   * </li>
   * </ul>
   */
  private void shell() {
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
            System.out.println("\twrite: reads memory at address");
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

        default:
          System.out.println("Unknown command " + cmd);
      }
    }
  }
}
