package microsim;

import microsim.DebugShell;
import microsim.component.*;
import java.io.*;
import java.util.*;

public class Main {

	// argument list:
	// -e: EPROM data path
	// -s: window scale
	// -d: debug mode
	static String getArgument(String[] args, String tag) {
		// step through arguments until tag is found
		for(int i = 0; i < args.length; i++) {
			if(tag.equals(args[i])) {
				// is the next argument in bounds?
				if(i + 1 >= args.length) {
					return null;
				} else {
					// return argument
					return args[i + 1];
				}
			}
		}

		return null;
	}
	static boolean getIfArgument(String[] args, String tag) {
		// step through arguments until tag is found
		for(int i = 0; i < args.length; i++) {
			if(tag.equals(args[i])) {
				return true;
			}
		}

		return false;
	}

	static byte[] loadEpromData(String path) throws IOException {
		System.out.println("Reading EPROM from " + path);

		// EPROM constants
		final int MAX_EPROM_SIZE = 27648; // set to EPROM_END - EPROM_BEG

		// initialize EPROM data array
		byte[] epromData = new byte[MAX_EPROM_SIZE];
		int idx = 0;

		// open EPROM data file
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line;

		// read by line
		while((line = reader.readLine()) != null) {
			line = line.trim();
			
			if(line.isEmpty()) continue; // empty line
			
			// read by token
			String[] tokens = line.split("\\s+"); // split by whitespace
			for(String token : tokens) {
				if(token.equals("//")) break; // comment
				
				// parse byte and insert
				byte value = (byte) Integer.parseInt(token, 16);
				epromData[idx++] = value;
			
				// System.out.println("Got byte: " + String.format("%02X", value & 0xFF));
			}
		}

		// close EPROM data file
		reader.close();	

		return epromData;
	}

	public static void main(String[] args)  throws InterruptedException, IOException {
		// read EPROM data
		String epromDataPath = getArgument(args, "-e");
		if(epromDataPath == null) {
			throw new RuntimeException("Please specify an EPROM data path with argument -e <eprom_data_path>");
		}
		byte[] epromData = loadEpromData(epromDataPath);

		// simulation setup
		// init bus
		Bus bus = new Bus();

		// init components on bus
		Processor proc = new Processor(bus);
		MemorySpace memory = new MemorySpace(bus, epromData);
		VideoDevice video = new VideoDevice(bus, memory);

		// set graphical window scale
		int windowScale = 1;
		
		// get scale if given
		String scaleArgument = getArgument(args, "-s");
		if(scaleArgument != null) {
			windowScale = Integer.parseInt(scaleArgument);
		}

		// init graphical window
		VideoWindow window = new VideoWindow(video, windowScale);

		// timing constants
		final int VIDEO_FREQ = 60;
		final int CPU_FREQ = 1000000; // 1 MHz
		final int FRAME_TIME = 1000 / VIDEO_FREQ;
		final int CYCLES_PER_FRAME = CPU_FREQ / VIDEO_FREQ;

		// cycle counters
		long cycle = 0;
		long nextFrameCycle = CYCLES_PER_FRAME;

		// get and set debug mode if given
		boolean debugMode = getIfArgument(args, "-d");
		DebugShell.debugMode = debugMode;
		DebugShell.setComponents(proc, memory);

		// show shell first time 
		DebugShell.shell();	

		// main simulation loop
		while(true) {
			// step through processor cycles
			while(cycle < nextFrameCycle) {
				DebugShell.log("Cycle " + cycle);
	
				// bus lines take their value
				bus.step();

				// components read and step
				proc.step();
				memory.step();
				video.step();

				// if debugging, stop and show debug shell
				DebugShell.shell();	

				cycle++;
			}

			// perform video update
			video.render();
			window.repaint();

			// set next video update cycle
			nextFrameCycle = cycle + CYCLES_PER_FRAME;
			
			// sleep until next video update
			Thread.sleep(FRAME_TIME);
		}
	}
}
