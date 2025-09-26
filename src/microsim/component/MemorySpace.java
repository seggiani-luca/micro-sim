package microsim.component;

public class MemorySpace implements RunnableComponent {
	private final int RAM_BEG 	= 0x0000;
	private final int RAM_END 	= 0x7fff;
	private final int VRAM_BEG 	= 0x8000;
	private final int VRAM_END 	= 0x8fff;
	private final int EPROM_BEG = 0x9000;
	private final int EPROM_END = 0xFFFF;

	private byte[] ram;
	private byte[] vram;
	private byte[] eprom;

	public MemorySpace() {
		ram = new byte[RAM_END - RAM_BEG];
		vram = new byte[VRAM_END - VRAM_BEG];
		eprom = new byte[EPROM_END - EPROM_BEG];
	}

	@Override
	public void step() {

	}

	public byte[] getVRAM() {
		return vram;
	}
}
