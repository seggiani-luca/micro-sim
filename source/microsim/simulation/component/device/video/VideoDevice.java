package microsim.simulation.component.device.video;

import microsim.simulation.component.bus.*;
import microsim.simulation.component.memory.*;
import microsim.simulation.event.*;
import microsim.simulation.component.device.ThreadedIoDevice;

/**
 * Implements a video device that renders a frame buffer by reading from VRAM. Actual rendering is
 * delegated to a {@link microsim.simulation.component.device.video.VideoRenderer} instance. Device
 * ports are 2 and used to set cursor position of the renderer instance.
 */
public class VideoDevice extends ThreadedIoDevice {

  /**
   * Frequency of video updates
   */
  public static final long FRAME_FREQ = 25;

  /**
   * Period of video updates.
   */
  public static final long FRAME_TIME = 1_000_000_000L / FRAME_FREQ;

  /**
   * The component in charge of actually holding and rendering a framebuffer.
   */
  private final VideoRenderer renderer;

  /**
   * Instantiates video device, taking a reference to the bus it's mounted on and the memory space
   * it should read VRAM from.
   *
   * @param bus bus the component is mounted on
   * @param base base memory offset
   * @param memory memory space to read from
   */
  public VideoDevice(Bus bus, int base, MemorySpace memory) {
    super(bus, base, 2);

    // init renderer
    renderer = new VideoRenderer(memory);
  }

  /**
   * Gets port (doesn't do anything for video device).
   *
   * @param index not significant
   * @return always 0
   */
  @Override
  public int getPort(int index) {
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
  public void setPort(int index, int data) {
    switch (index) {
      case 0 ->
        renderer.setCursorRow(data);
      case 1 ->
        renderer.setCursorColumn(data);
    }
  }

  /**
   * Implements the thread that refreshes the video renderer.
   */
  @Override
  protected void deviceThread() {
    long frameTime = System.nanoTime();
    while (true) {
      // render to buffer
      render();

      // wait for frame time
      frameTime += FRAME_TIME;
      smartSpin(frameTime);
    }
  }

  /**
   * Queries the video render to render a frame and signals it to interfaces. This is meant to be
   * called by the device thread, and by the debug shell to force frame renders.
   */
  public void render() {
    renderer.render();

    // raise frame event to notify interfaces
    raiseEvent(new FrameEvent(this, renderer.getFrame()));
  }

}
