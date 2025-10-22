package microsim.simulation.component.device.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import microsim.simulation.component.memory.*;
import microsim.simulation.info.VideoInfo;

/**
 * Implements a rendering component for the
 * {@link microsim.simulation.component.device.video.VideoDevice} device, responsible of keeping a
 * frame buffer and rendering to it. Functions in text mode, with bitmap characters read from a file
 * and loaded into a buffered image cache. Also handles cursor drawing and blinking.
 */
public final class VideoRenderer {

  /**
   * Video device info this renderer implements (renderer is always part of a video device).
   */
  VideoInfo info;

  /**
   * Character atlas, read from file specified in configuration.
   */
  private static BufferedImage[][] charAtlas;

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
    return info.cols * info.charWidth;
  }

  /**
   * Calculates and returns frame buffer height.
   *
   * @return frame buffer height
   */
  public int getFrameHeight() {
    return info.rows * info.charHeight;
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
   * Instantiates video renderer, taking a reference to the memory space it should read VRAM from.
   *
   * @param info video info to build renderer from
   */
  public VideoRenderer(VideoInfo info) {
    this.info = info;

    // fetch character atlas
    try {
      // load full atlas
      BufferedImage charsetBmp = ImageIO.read(new File(info.charsetPath));

      // split atlas in characters for quicker lookup
      charAtlas = new BufferedImage[info.charsetSize][256 / info.charsetSize];

      for (int x = 0; x < info.charsetSize; x++) {
        for (int y = 0; y < 256 / info.charsetSize; y++) {
          charAtlas[x][y] = charsetBmp.getSubimage(
                  x * info.charWidth,
                  y * info.charHeight,
                  info.charWidth,
                  info.charHeight
          );
        }
      }
    } catch (IOException e) {
      System.out.println("Error loading character atlas. " + e.getMessage());
      System.exit(1);
    }

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
    for (int r = 0; r < info.rows; r++) {
      for (int c = 0; c < info.cols; c++) {
        int addr = r * info.cols + c;

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
        g.drawImage(charImage, c * info.charWidth, r * info.charHeight, null);
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
    int x = ch % info.charsetSize;
    int y = ch / (256 / info.charsetSize);

    // get character from charAtlas
    BufferedImage charImage = charAtlas[x][y];

    return charImage;
  }
}
