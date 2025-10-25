package microsim.ui;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import microsim.simulation.component.device.video.*;
import microsim.simulation.event.*;

/**
 * Extends JPanel to display frame buffers rendered by
 * {@link simulation.component.device.video.VideoDevice} components.
 */
class VideoPanel extends JPanel {

  /**
   * Scale of the panel. 1x matches to display pixels, 2x looks better on most displays (especially
   * HiDPI).
   */
  private final int scale;

  /**
   * The frame buffer to display. Gets updated on {@link #updateFrame(java.awt.image.BufferedImage)}
   * and rendered to the panel on {@link #paint(java.awt.Graphics)} calls.
   */
  private BufferedImage frame;

  /**
   * Instantiates panel, setting preferred size to scaled frame buffer size. Frame buffer size is
   * obtained from a {@link simulation.component.device.video.VideoDevice} instance.
   *
   * @param video video device to get frame buffer size from
   * @param scale scale factor of the frame buffer
   */
  public VideoPanel(VideoDevice video, int scale) {
    this.scale = scale;

    int panelWidth = video.getRenderer().getFrameWidth();
    int panelHeight = video.getRenderer().getFrameHeight();

    // init placeholder frame, this will be rendered to by video device
    frame = new BufferedImage(
            panelWidth,
            panelHeight,
            BufferedImage.TYPE_INT_RGB
    );

    Graphics g = frame.getGraphics();
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(0, 0, frame.getWidth(), frame.getHeight());

    g.dispose();

    // set preferred size
    setPreferredSize(new Dimension(
            panelWidth * scale,
            panelHeight * scale
    ));
  }

  /**
   * Updates the frame buffer. Gets called on FrameEvents raised by the
   * {@link simulation.component.device.video.VideoDevice} component attached to the main video
   * window (which should be the same that the panel was initialized on).
   *
   * @param frame the new frame buffer
   */
  public void updateFrame(BufferedImage frame) {
    // invoke from swing utilities to make sure updates happen on the EDT thread
    SwingUtilities.invokeLater(() -> {
      this.frame = frame;
      repaint();
    });
  }

  /**
   * Override of JPanel's paintComponent method that scales frame buffer and paints it to the panel.
   * Uses nearest neighbor scaling for crisp pixels.
   *
   * @param g the Graphics object to protect (see JComponent's documentation)
   */
  @Override
  protected void paintComponent(Graphics g) {
    // paint JPanel
    super.paintComponent(g);

    // use Graphics2D for scaling
    Graphics2D g2 = (Graphics2D) g;

    // use nearest neighbor for scaling
    g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
    );

    // get scaled dimensions
    int w = frame.getWidth() * scale;
    int h = frame.getHeight() * scale;

    // draw scaled frame
    g2.drawImage(frame, 0, 0, w, h, null);
  }
}

/**
 * Handles JFrame for windowing and {@link microsim.ui.VideoPanel} for frame buffer display. Is
 * attached to a specific {@link microsim.simulation.component.device.video.VideoDevice} instance
 * (given to the constructor), which it first uses to initialize panel dimensions. From there
 * onwards, it only responds to frame events raised by that instance.
 */
public class VideoWindow implements SimulationListener {

  /**
   * Main window JFrame.
   */
  private final JFrame frame;

  /**
   * VideoPanel displaying frame buffer.
   */
  private final VideoPanel panel;

  /**
   * Returns panel, for attaching input (e.g. keyboard input grabbed from panel).
   *
   * @return JPanel instance
   */
  public JPanel getPanel() {
    return panel;
  }

  /**
   * Instantiates window and VideoPanel. Window resizing is disabled so that window size is locked
   * to scaled frame buffer size.
   *
   * @param video video device to render
   * @param scale scale factor of the frame buffer
   * @param title title of window
   */
  public VideoWindow(VideoDevice video, int scale, String title) {
    // setup panel
    panel = new VideoPanel(video, scale);

    // setup window
    frame = new JFrame("micro-sim: " + title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);

    frame.add(panel);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Receives {@link microsim.simulation.event.FrameEvent} events and uses them to update
   * {@link #panel}'s frame buffer. Also checks for {@link microsim.simulation.event.HaltEvent} to
   * close window.
   */
  @Override
  public void onSimulationEvent(SimulationEvent e) {
    // check for frame event
    if (e instanceof FrameEvent f) {
      // have panel update frame
      panel.updateFrame(f.frame);
    }

    // check for halt event
    if (e instanceof HaltEvent) {
      frame.dispose();
    }
  }
}
