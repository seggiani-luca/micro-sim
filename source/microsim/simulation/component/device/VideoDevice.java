package microsim.simulation.component.device;

import microsim.simulation.component.MemorySpace;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import microsim.simulation.component.Bus;
import microsim.simulation.event.*;

/**
 * Implements a video device that renders a frame buffer by reading from VRAM. Functions in text
 * mode, with bitmap characters read from a file and loaded into a buffered image cache. Device
 * ports are 2 and used to set the cursor position.
 */
public class VideoDevice extends IoDevice {

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
  private static BufferedImage[][] charAtlas;

  // try to fetch charAtlas
  static {
    try {
      // load full atlas
      BufferedImage charAtlasBmp = ImageIO.read(new File("assets/charAtlas.bmp"));

      // split atlas in characters for quicker lookup
      charAtlas = new BufferedImage[ATLAS_SIZE][256 / ATLAS_SIZE];

      for (int x = 0; x < ATLAS_SIZE; x++) {
        for (int y = 0; y < 256 / ATLAS_SIZE; y++) {
          charAtlas[x][y] = charAtlasBmp.getSubimage(
            x * CHAR_WIDTH,
            y * CHAR_HEIGHT,
            CHAR_WIDTH,
            CHAR_HEIGHT
          );
        }
      }
    } catch (IOException e) {
      System.out.println("Error loading character atlas: " + e.getMessage());
      System.exit(1);
    }
  }

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
   * Row of cursor.
   */
  private int cursorRow;

  /**
   * Column of cursor.
   */
  private int cursorColumn;

  /**
   * Used to keep track of cursor blink state.
   */
  private boolean blink;

  /**
   * Times consecutive blinks.
   */
  private int blinkTimer;

  /**
   * Frame updates between blink state changes.
   */
  private static final int BLINK_TIME = 10;

  /**
   * Instantiates video device, taking a reference to the bus it's mounted on and the memory space
   * it should read VRAM from.
   *
   * @param bus bus the component is mounted on
   * @param memory memory space to read from
   */
  public VideoDevice(Bus bus, MemorySpace memory) {
    super(bus, 0x00030000, 2);
    this.memory = memory;

    // init frame buffer
    frame = new BufferedImage(
      getFrameWidth(),
      getFrameHeight(),
      BufferedImage.TYPE_INT_RGB
    );
  }

  /**
   * Gets port (doesn't do anything for video device).
   *
   * @param index not significant
   * @return always 0
   */
  @Override
  int getPort(int index) {
    // nothing to return
    return 0;
  }

  /**
   * Sets cursor ports. Port 0 is row and port 1 is column.
   *
   * @param index index of port
   * @param data value to give port
   */
  @Override
  void setPort(int index, int data) {
    switch (index) {
      case 0 ->
        cursorRow = data;
      case 1 ->
        cursorColumn = data;
    }
  }

  /**
   * Renders frame buffer from read VRAM. Characters in VRAM are comprised of a bytes representing
   * extended ASCII code point.
   */
  public void render() {
    // update blink
    blinkTimer++;
    if (blinkTimer == BLINK_TIME) {
      blinkTimer = 0;
      blink = !blink;
    }

    // directly read VRAM
    byte[] vram = memory.getVRAM();

    // get frame buffer Graphics and clear
    var g = frame.getGraphics();
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(0, 0, frame.getWidth(), frame.getHeight());

    // step through VRAM and paint characters
    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        int addr = r * COLS + c;

        // char codepoint, underscore by default
        byte ch = '_';

        if (r == cursorRow && c == cursorColumn) {
          // blink
          if (blink) {
            ch = vram[addr];
          }
        } else {
          // don't blink
          ch = vram[addr];
        }

        // get character and paint
        BufferedImage charImage = getCharSprite(ch);
        g.drawImage(charImage, c * CHAR_WIDTH, r * CHAR_HEIGHT, null);
      }
    }

    g.dispose();

    // raise frame event to notify interfaces
    raiseEvent(new FrameEvent(this, frame));
  }

  /**
   * Gets a character from code point.
   *
   * @param ch character code point
   * @return character image
   */
  private BufferedImage getCharSprite(byte ch) {
    // get character coordinates on charAtlas
    int x = ch % ATLAS_SIZE;
    int y = ch / ATLAS_SIZE;

    // get character from charAtlas
    BufferedImage charImage = charAtlas[x][y];

    return charImage;
  }
}
