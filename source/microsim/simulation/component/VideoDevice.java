package microsim.simulation.component;

import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import microsim.simulation.event.*;

public class VideoDevice extends SimulationComponent {

  static final int COLS = 80;
  static final int ROWS = 30;
  static final int CHAR_WIDTH = 8;
  static final int CHAR_HEIGHT = 16;

  private static final int ATLAS_SIZE = 16;

  private static BufferedImage charAtlas;

  // try to fetch charAtlas
  static {
    try {
      charAtlas = ImageIO.read(new File("assets/charAtlas.bmp"));
    } catch (IOException e) {
      e.printStackTrace();
      charAtlas = null;
    }
  }

  private final Bus bus;
  private final MemorySpace memory; // reference to memory for direct VRAM reads

  private final BufferedImage frame;

  /**
   * Calculates and returns frame buffer width.
   *
   * @return frame buffer width
   */
  public static int getFrameWidth() {
    return COLS * CHAR_WIDTH;
  }

  /**
   * Calculates and returns frame buffer height.
   *
   * @return frame buffer height
   */
  public static int getFrameHeight() {
    return ROWS * CHAR_HEIGHT;
  }

  public VideoDevice(Bus bus, MemorySpace memory) {
    this.bus = bus;
    this.memory = memory;

    // init frame buffer
    frame = new BufferedImage(
      getFrameWidth(),
      getFrameHeight(),
      BufferedImage.TYPE_INT_RGB
    );
  }

  @Override
  public void step() {

  }

  public void render() {
    // directly read VRAM
    byte[] vram = memory.getVRAM();

    // get frame buffer Graphics and clear
    var g = frame.getGraphics();
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(0, 0, frame.getWidth(), frame.getHeight());

    // step through VRAM and paint characters
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        int addr = (r * COLS + c) * 2;

        // character codepoint
        byte ch = vram[addr];

        // character style attributes
        byte st = vram[addr + 1];

        // get character and paint
        BufferedImage charImage = getCharSprite(ch, st);
        g.drawImage(charImage, c * CHAR_WIDTH, r * CHAR_HEIGHT, null);
      }
    }

    g.dispose();

    // raise frame event to notify interfaces
    raiseEvent(new FrameEvent(this, frame));
  }

  private BufferedImage getCharSprite(byte ch, byte st) {
    // get character coordinates on charAtlas
    int x = ch % ATLAS_SIZE;
    int y = ch / ATLAS_SIZE;

    // get character from charAtlas
    BufferedImage charImage = charAtlas.getSubimage(
      x * CHAR_WIDTH,
      y * CHAR_HEIGHT,
      CHAR_WIDTH,
      CHAR_HEIGHT
    );

    // copy character to allow recoloring
    BufferedImage charImageCopy = new BufferedImage(
      CHAR_WIDTH,
      CHAR_HEIGHT,
      BufferedImage.TYPE_INT_RGB
    );
    charImageCopy.getGraphics().drawImage(charImage, 0, 0, null);

    return charImageCopy;
  }

}
