package microsim.simulation.component;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import microsim.simulation.event.*;

/**
 * Implements a video device that renders a frame buffer by reading from VRAM. Functions in text
 * mode, with bitmap characters read from a file.
 */
public class VideoDevice extends SimulationComponent {

  /**
   * Number of columns in text mode.
   */
  static final int COLS = 80;

  /**
   * Number of rows in text mode.
   */
  static final int ROWS = 30;

  /**
   * Width of character in text mode.
   */
  static final int CHAR_WIDTH = 8;

  /**
   * Height of character in text mode.
   */
  static final int CHAR_HEIGHT = 16;

  /**
   * Width (in characters) of the character atlas. Height is not needed as we are targeting 256
   * character extended ASCII.
   */
  private static final int ATLAS_SIZE = 16;

  /**
   * Character atlas, read from file "assets/charAtlas.bmp".
   */
  private static BufferedImage charAtlas;

  // try to fetch charAtlas
  static {
    try {
      charAtlas = ImageIO.read(new File("assets/charAtlas.bmp"));
    } catch (IOException e) {
      System.out.println("Error loading character atlas: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Reference to the communication bus the component is mounted on.
   */
  private final Bus bus;
  /**
   * Reference to memory space. Used to directly access VRAM via the
   * {@link microsim.simulation.component.MemorySpace#getVRAM()} method.
   */
  private final MemorySpace memory;

  /**
   * Frame buffer to render on.
   */
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

  /**
   * Instantiates video device, taking a reference to the bus it's mounted on and the memory space
   * it should read VRAM from.
   *
   * @param bus bus the component is mounted on
   * @param memory memory space to read from
   */
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

  /**
   * Currently unused by video device. TODO: implement cursor functionality reading from ports.
   */
  @Override
  public void step() {

  }

  /**
   * Renders frame buffer from read VRAM. Characters in VRAM are comprised of 2 bytes, with higher
   * byte representing extended ASCII code point, and lower byte representing style.
   */
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

  /**
   * Gets a character from code point and style byte.
   *
   * @param ch character code point
   * @param st style byte
   * @return character image
   */
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
