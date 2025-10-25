package microsim.simulation.component.device.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import microsim.simulation.component.memory.*;

/**
 * Implements a rendering component for the
 * {@link microsim.simulation.component.device.video.VideoDevice} device, responsible of keeping a
 * frame buffer and rendering to it. Functions in text mode, with bitmap characters read from a file
 * and loaded into a buffered image cache. Also handles cursor drawing and blinking.
 */
public final class VideoRenderer {

  /**
   * Number of columns in text mode.
   */
  public static final int COLS = 80;

  /**
   * Number of rows in text mode.
   */
  public static final int ROWS = 30;

  /**
   * Width of character in text mode.
   */
  public static final int CHAR_WIDTH = 8;

  /**
   * Height of character in text mode.
   */
  public static final int CHAR_HEIGHT = 16;

  /**
   * Path of character set image.
   */
  public static final String CHARSET_PATH = "data/charset8x16sans.bmp";

  /**
   * Width (in characters) of the character set image. Height is not needed as we are targeting 256
   * character extended ASCII.
   */
  public static final int CHARSET_SIZE = 16;

  /**
   * Character atlas, read from file specified in configuration.
   */
  private static BufferedImage[][] charAtlas = null;

  static {
    // fetch character atlas
    try {
      // load full atlas
      BufferedImage charsetBmp = ImageIO.read(new File(CHARSET_PATH));

      // split atlas in characters for quicker lookup
      charAtlas = new BufferedImage[CHARSET_SIZE][256 / CHARSET_SIZE];

      for (int x = 0; x < CHARSET_SIZE; x++) {
        for (int y = 0; y < 256 / CHARSET_SIZE; y++) {
          charAtlas[x][y] = charsetBmp.getSubimage(
                  x * CHAR_WIDTH,
                  y * CHAR_HEIGHT,
                  CHAR_WIDTH,
                  CHAR_HEIGHT
          );
        }
      }
    } catch (IOException e) {
      System.out.println("Error loading character atlas. " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Reference to memory space. Used to directly access VRAM via the
   * {@link simulation.component.memory.MemorySpace#getVRAM()} method.
   */
  private MemorySpace memory;

  /**
   * Attaches a memory space to this renderer. Used to defer memory attachment after renderer has
   * been built.
   *
   * @param memory memory to attach
   */
  public void attachMemory(MemorySpace memory) {
    this.memory = memory;
  }

  /**
   * Frame buffer to render on.
   */
  private final BufferedImage frame;

  /*
   * Gets the held frame buffer.
   */
  public BufferedImage getFrame() {
    return frame;
  }

  /**
   * Calculates and returns frame buffer width.
   *
   * @return frame buffer width
   */
  public int getFrameWidth() {
    return COLS * CHAR_WIDTH;
  }

  /**
   * Calculates and returns frame buffer height.
   *
   * @return frame buffer height
   */
  public int getFrameHeight() {
    return ROWS * CHAR_HEIGHT;
  }

  /**
   * Row of cursor.
   */
  private int cursorRow;

  /**
   * Sets the row of the cursor.
   *
   * @param val row to update to
   */
  public void setCursorRow(int val) {
    cursorRow = val;
  }

  /**
   * Column of cursor.
   */
  private int cursorColumn;

  /**
   * Sets the column of the cursor.
   *
   * @param val column to update to
   */
  public void setCursorColumn(int val) {
    cursorColumn = val;
  }

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
   * Instantiates video renderer.
   */
  public VideoRenderer() {
    // init frame buffer
    frame = new BufferedImage(
            getFrameWidth(),
            getFrameHeight(),
            BufferedImage.TYPE_INT_RGB
    );
  }

  /**
   * Renders frame buffer from read VRAM. Characters in VRAM are comprised of a bytes representing
   * extended ASCII code points. If the cursor coordinates are within the frame buffer, it is drawn
   * or not depending on the current blink timer.
   */
  public void render() {
    // memory must be attached
    if (memory == null) {
      throw new RuntimeException("Tried rendering with no memory device attached");
    }

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
  }

  /**
   * Gets a buffered image representing a character from its extended ASCII code point.
   *
   * @param ch character code point
   * @return character image
   */
  private BufferedImage getCharSprite(int ch) {
    // mask to get unsigned behavoir
    ch &= 0xff;

    // get character coordinates on charAtlas
    int x = ch % CHARSET_SIZE;
    int y = ch / (256 / CHARSET_SIZE);

    // get character from charAtlas
    BufferedImage charImage = charAtlas[x][y];

    return charImage;
  }
}
