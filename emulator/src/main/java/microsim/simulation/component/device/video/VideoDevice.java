package microsim.simulation.component.device.video;

import microsim.simulation.component.bus.*;
import microsim.simulation.component.memory.*;
import microsim.simulation.event.*;
import microsim.simulation.info.VideoInfo;
import microsim.simulation.component.device.ThreadedIoDevice;

/**
 * Implements a video device that renders a frame buffer by reading from VRAM. Actual rendering is
 * delegated to a {@link microsim.simulation.component.device.video.VideoRenderer} instance. Device
 * ports are 2 and used to set cursor position of the renderer instance.
 */
public class VideoDevice extends ThreadedIoDevice {

  /**
   * Video device info this component implements.
   */
  @SuppressWarnings("unused")
  private final VideoInfo info; // currently unused

  /**
   * Period of video updates (calculated from update frequency).
   */
  public final long frameTime;

  /**
   * The component in charge of actually holding and rendering a framebuffer.
   */
  private final VideoRenderer renderer;

  /**
   * Attaches a memory space to the owned renderer. Used to defer memory attachment after renderer
   * has been built.
   *
   * @param memory memory to attach
   */
  public void attachMemory(MemorySpace memory) {
    renderer.attachMemory(memory);
  }

  /**
   * Gets this video device's renderer.
   *
   * @return this video device's renderer
   */
  public VideoRenderer getRenderer() {
    return renderer;
  }

  /**
   * Instantiates video device, taking a reference to the bus it's mounted on and the memory space
   * it should read VRAM from.
   *
   * @param bus bus the component is mounted on
   * @param info info to build video device from
   */
  public VideoDevice(Bus bus, VideoInfo info) {
    super(bus, info.base, 2);
    this.info = info;

    frameTime = 1_000_000_000 / info.frameFreq; // in ns

    // init renderer
    renderer = new VideoRenderer(info);
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
    long updateTime = System.nanoTime();
    while (true) {
      // render to buffer
      render();

      // wait for frame time
      updateTime += frameTime;
      smartSpin(updateTime);
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
