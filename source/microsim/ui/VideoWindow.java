package microsim.ui;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import microsim.simulation.component.device.video.*;
import microsim.simulation.event.*;

/**
 * Extends JPanel to display frame buffers rendered by
 * {@link microsim.simulation.component.device.video.VideoDevice} components.
 */
class VideoPanel extends JPanel {

  /**
   * Scale of the panel. 1x matches to display pixels, 2x looks better on most displays.
   */
  private final int scale;

  /**
   * The frame buffer to display. Gets updated on FrameEvents raised by
   * {@link microsim.simulation.component.device.video.VideoDevice} components.
   */
  private BufferedImage frame;

  /**
   * Instantiates panel, setting preferred size to scaled frame buffer size. Frame buffer size is
   * obtained from {@link microsim.simulation.component.device.video.VideoDevice}.
   *
   * @param scale scale factor of the frame buffer
   */
  public VideoPanel(int scale) {
    this.scale = scale;

    // init placeholder frame
    frame = new BufferedImage(
      VideoRenderer.getFrameWidth(),
      VideoRenderer.getFrameHeight(),
      BufferedImage.TYPE_INT_RGB
    );

    var g = frame.getGraphics();
    g.setColor(java.awt.Color.BLACK);
    g.fillRect(0, 0, frame.getWidth(), frame.getHeight());

    g.dispose();

    // set preferred size
    setPreferredSize(new Dimension(
      VideoRenderer.getFrameWidth() * scale,
      VideoRenderer.getFrameHeight() * scale
    ));
  }

  /**
   * Updates the frame buffer.
   *
   * @param frame the new frame buffer
   */
  public void updateFrame(BufferedImage frame) {
    this.frame = frame;
  }

  /**
   * Override of JPanel paintComponent method that scales frame buffer. Uses nearest neighbor
   * scaling for crisp pixels.
   *
   * @param g the Graphics object to protect
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
 * Handles JFrame for windowing and {@link microsim.ui.VideoPanel} for frame buffer display.
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
   * Returns panel, for attaching input.
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
   * @param scale scale factor of the frame buffer
   */
  public VideoWindow(int scale) {
    // setup panel
    panel = new VideoPanel(scale);

    // setup window
    frame = new JFrame("micro-sim");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);

    frame.add(panel);
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Receives {@link microsim.simulation.event.FrameEvent} events and uses them to repaint
   * {@link #panel}.
   */
  @Override
  public void onSimulationEvent(SimulationEvent e) {
    // check for FrameEvent
    if (e instanceof FrameEvent frameEvent) {
      panel.updateFrame(frameEvent.frame);
      panel.repaint();
    }
  }
}
