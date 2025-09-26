package microsim.component;

import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class VideoDevice implements RunnableComponent {
	private static final int COLS = 80;
	private static final int ROWS = 30;
	private static final int CHAR_WIDTH = 8;
	private static final int CHAR_HEIGHT = 16;

	private static final int ATLAS_SIZE = 16;

	private static BufferedImage charAtlas;

	static {
		try {
 			charAtlas = ImageIO.read(new File("assets/charAtlas.bmp"));
		} catch(IOException e) {
			e.printStackTrace();
			charAtlas = null;
		}
	}
	
	private final MemorySpace memory;
	private final BufferedImage frame;

	public VideoDevice(MemorySpace memory) {
		this.memory = memory;
	
		frame = new BufferedImage(
			COLS * CHAR_WIDTH,
			ROWS * CHAR_HEIGHT,
			BufferedImage.TYPE_INT_RGB
		);
	}

	@Override
	public void step() {

	}

	public void render() {
		byte[] vram = memory.getVRAM();
		
		var g = frame.getGraphics();
		g.setColor(java.awt.Color.BLACK);
		g.fillRect(0, 0, frame.getWidth(), frame.getHeight());

		for(int r = 0; r < ROWS; r++) {
			for(int c = 0; c < COLS; c++) {
				int addr = (r * COLS + c) * 2;
				
				byte ch = vram[addr];
				byte st = vram[addr + 1];
			
				BufferedImage charImage = getCharSprite(ch, st);
				g.drawImage(charImage, c * CHAR_WIDTH, r * CHAR_HEIGHT, null);
				g.dispose();
			}
		}
	}

	private BufferedImage getCharSprite(byte ch, byte st) {
		int x = ch % ATLAS_SIZE;
		int y = ch / ATLAS_SIZE;

		BufferedImage charImage = charAtlas.getSubimage(
			x * CHAR_WIDTH, 
			y * CHAR_HEIGHT, 
			CHAR_WIDTH, 
			CHAR_HEIGHT
		);

		BufferedImage charImageCopy = new BufferedImage(
			CHAR_WIDTH, 
			CHAR_HEIGHT, 
			BufferedImage.TYPE_INT_RGB);
		charImageCopy.getGraphics().drawImage(charImage, 0, 0, null);

		return charImageCopy;
	}

	public BufferedImage getFrame() {
		return frame;
	}
}
