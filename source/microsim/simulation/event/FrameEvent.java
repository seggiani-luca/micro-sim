package microsim.simulation.event;

import java.awt.image.*;
import microsim.simulation.component.*;

/**
 * SimulationEvent that signals a {@link microsim.simulation.component.VideoDevice} has finished
 * rendering to frame buffer
 */
public class FrameEvent extends SimulationEvent {

  /**
   * Frame buffer of the {@link microsim.simulation.component.VideoDevice} that finished rendering
   */
  public BufferedImage frame;

  /**
   * Instantiates FrameEvent getting a reference to the VideoDevice that raised it. Also gets a
   * reference to the frame buffer.
   *
   * @param owner VideoDevice that raised FrameEvent
   * @param frame rendered frame
   */
  public FrameEvent(VideoDevice owner, BufferedImage frame) {
    super(owner);
    this.frame = frame;
  }

  @Override
  public String getDebugMessage() {
    return "Video device rendered frame";
  }

}
