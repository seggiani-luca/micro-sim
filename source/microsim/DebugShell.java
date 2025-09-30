package microsim;

import microsim.component.*;
import java.util.*;

public class DebugShell {
	static boolean debugMode;
	
	static Processor proc;
	static MemorySpace memory;

	public static void setComponents(Processor proc, MemorySpace memory) {
		DebugShell.proc = proc;
		DebugShell.memory = memory;
	}

	public static void printProcessorRegisters() {
		char[] registers = proc.getRegisters();
		
		System.out.println("\t%a:\t" + String.format("%04X", registers[0] & 0xffff));
		System.out.println("\t%b:\t" + String.format("%04X", registers[1] & 0xffff));
		System.out.println("\t%c:\t" + String.format("%04X", registers[2] & 0xffff));
		System.out.println("\t%d:\t"  + String.format("%04X", registers[3] & 0xffff));
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

	public static void printProcessorState() {
		Processor.ProcessorState state = proc.getState();

		System.out.println("\tPresent state is: " + state.name());
	}

	public static void log(String message) {
		if(debugMode) {
			message = message.replace("\n", "\n\t");
			System.out.println("\t" + message);
		}
	}

	public static void shell() {
		if(!debugMode) return;

		while(true) {
			System.out.print("debug> ");
			String cmd = new java.util.Scanner(System.in).nextLine().strip();

			if(cmd.isEmpty()) {
				System.out.println("Available commands:");
				System.out.println("\tstep: steps execution by 1 cycle");
				System.out.println("\tquit: halts executions and quits");
				System.out.println("\tproc: offers processor information");
				System.out.println("\tmem: offers memory information");
				continue;
			}

			String[] tokens = cmd.split("\\s+");

			switch(tokens[0]) {
				case "s":
				case "step":
					return;

				case "q":
				case "quit":
					System.exit(0);

				case "p":
				case "proc": {
					if(tokens.length < 2) {
						System.out.println("Available proc options:");
						System.out.println("\tregisters: prints all registers");
						System.out.println("\tstate: prints state information");
						continue;
					}
					switch(tokens[1]) {
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
					if(tokens.length < 2) {
						System.out.println("Available mem options:");
						System.out.println("\tread: reads memory at address");
						continue;
					}
					switch(tokens[1]) {
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
