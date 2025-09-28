package microsim.component;

public class MemorySpace implements RunnableComponent {
	// memory space layout
	private static final int RAM_BEG = 0x0000; // 32 KiB
	private static final int RAM_END = 0x7fff;
	private static final int VRAM_BEG = 0x8000; // 5 KiB
	private static final int VRAM_END = 0x93ff;
	private static final int EPROM_BEG = 0x9400; // 27 KiB
	private static final int EPROM_END = 0xFFFF;

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

			System.out.println("Memory saw read operation at address " + String.format("%04X", addr & 0xFFFF));
		
			// get word in two byte reads
			byte dataHi = readMemory(addr);
			byte dataLow = readMemory((char)((addr + 1) % EPROM_END));
		
			// rebuild word
			char data = (char)((dataHi << 8) | dataLow);
			
			System.out.println("Memory read value " + String.format("%04X", data & 0xFFFF) + " from given address");

			// drive data line with word
			bus.dataLine.drive(this, data);
			driving = true;
			
			return;
		}

		if(writeEnable) {
			// write operation
			
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

	// used by VideoDevice for direct reads
	public byte[] getVRAM() {
		return vram;
	}
}
