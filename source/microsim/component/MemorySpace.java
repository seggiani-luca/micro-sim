package microsim.component;

import java.util.Arrays;

public class MemorySpace implements RunnableComponent {
	// memory space layout
	private static final int RAM_BEG = 0x0000; // 32 KiB
	private static final int RAM_END = 0x7fff;
	private static final int VRAM_BEG = 0x8000; // 5 KiB
	private static final int VRAM_END = 0x93ff;
	private static final int EPROM_BEG = 0x9400; // 27 KiB
	private static final int EPROM_END = 0xffff;

	// one vector for each subspace
	private byte[] ram;
	private byte[] vram;
	private byte[] eprom;

	private final Bus bus;
	private boolean driving;

	public MemorySpace(Bus bus, byte[] epromData) {
		this.bus = bus;

		// setup memory arrays
		ram = new byte[RAM_END - RAM_BEG + 1];
		vram = new byte[VRAM_END - VRAM_BEG + 1];
		eprom = new byte[EPROM_END - EPROM_BEG + 1];
		
		// read EPROM data into memory
		if(epromData.length > eprom.length) {
			throw new RuntimeException("Given EPROM data doesn't fit in EPROM");
		}
	
		System.arraycopy(epromData, 0, eprom, 0, epromData.length);

		// System.out.println("EPROM contents after initialization are:");
		// for (byte b : epromData) {
  	// 	System.out.println(String.format("%02X ", b & 0xFF));
		// }
	}

	@Override
	public void step() {
		if(bus.targetSpace.read() != false) {
			// not targeting ram
			return;
		}
		
		boolean readEnable = bus.readEnable.read();
		boolean writeEnable = bus.writeEnable.read();
	
		if(readEnable && writeEnable) {
			throw new RuntimeException("Read Enable and Write Enable simultaneously high");
		}

		if(readEnable) {
			// read operation
			char addr = bus.addressLine.read();

			System.out.println("Memory saw read operation at address " + String.format("%04X", addr & 0xffff));
		
			// get word in two byte reads
			byte dataHi = readMemory(addr);
			byte dataLow = readMemory((char)((addr + 1) % EPROM_END));
	
			System.out.println("Memory read high word " + String.format("%02X", dataHi & 0xff));
			System.out.println("Memory read low word " + String.format("%02X", dataLow % 0xff));

			// rebuild word
			char data = (char)(((dataHi & 0xff) << 8) | (dataLow & 0xff));
			
			System.out.println("Memory read value " + String.format("%04X", data & 0xffff) + " from given address");

			// drive data line with word
			bus.dataLine.drive(this, data);
			driving = true;
			
			return;
		}

		if(writeEnable) {
			// write operation
			char addr = bus.addressLine.read();
			char data = bus.dataLine.read();

			System.out.println("Memory saw write operation at address " + String.format("%04X", addr & 0xffff) + " of value " + String.format("%04X", data & 0xffff));

			byte dataHi = (byte)((data >> 8) & 0xff);
			byte dataLow = (byte)(data & 0xff);

			writeMemory(addr, dataHi);
			writeMemory((char)((addr + 1) % EPROM_END), dataLow);

			return;
		}

		// release if driving
		if(driving) {
			bus.dataLine.release(this);
			driving = false;
		}
	}

	private byte readMemory(char addr) {
		if(addr >= RAM_BEG && addr <= RAM_END) {
			return ram[addr - RAM_BEG];
		} else if(addr >= VRAM_BEG && addr <= VRAM_END) {
			return vram[addr - VRAM_BEG];
		} else if(addr >= EPROM_BEG && addr <= EPROM_END) {
			return eprom[addr - EPROM_BEG];
		}

		throw new RuntimeException("Memory read out of bounds");
	}

	private void writeMemory(char addr, byte data) {
		if(addr >= RAM_BEG && addr <= RAM_END) {
			ram[addr - RAM_BEG] = data;
			return;
		} else if(addr >= VRAM_BEG && addr <= VRAM_END) {
			vram[addr - VRAM_BEG] = data;
			return;
		} else if(addr >= EPROM_BEG && addr <= EPROM_END) {
			eprom[addr - EPROM_BEG] = data;
			return;
		}

		throw new RuntimeException("Memory write out of bounds");
	}

	// used by VideoDevice for direct reads
	public byte[] getVRAM() {
		return vram;
	}
}
