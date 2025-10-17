package microsim.simulation;

import java.util.LinkedList;
import java.util.List;
import microsim.simulation.component.*;
import microsim.simulation.component.bus.*;
import microsim.simulation.component.device.*;
import microsim.simulation.component.device.keyboard.*;
import microsim.simulation.component.device.timer.*;
import microsim.simulation.component.device.video.*;
import microsim.simulation.component.memory.*;
import microsim.simulation.component.processor.*;
import microsim.simulation.event.*;
import microsim.simulation.info.*;
import microsim.ui.DebugShell;

/**
 * Represents a simulation instance. Contains references to simulated components and timing
 * constants. Pipes {@link microsim.simulation.event.FrameEvent} events from
 * {@link microsim.simulation.component.SimulationComponent} components to external
 * {@link microsim.simulation.event.SimulationListener} listeners (usually interfaces).
 */
public class Simulation extends SimulationComponent implements SimulationListener {

  /**
   * Simulated processor component. Instantiated after bus, updated first in simulation steps.
   */
  public Processor proc;

  /**
   * Simulated memory component. Instantiated after bus, updated second in simulation steps.
   */
  public MemorySpace memory;

  /**
   * List of simulated devices. Instantiated after bus, updated third in simulation steps.
   */
  public List<IoDevice> devices = new LinkedList<>();

  /**
   * Returns first device by type in device list. If not found returns null.
   *
   * @param <T> type of device instance to return
   * @param type class of device instance to find (matches above)
   * @return first found device instance of class type
   */
  public <T extends IoDevice> T getDevice(Class<T> type) {
    for (IoDevice device : devices) {
      if (type.isInstance(device)) {
        return type.cast(device);
      }
    }

    return null;
  }

  /**
   * Instantiates simulation, loading EPROM data in memory and configuring devices and components.
   * Sets self as listener of the simulation components involved.
   *
   * @param info simulation info for configuration
   */
  @SuppressWarnings("LeakingThisInConstructor")
  public Simulation(SimulationInfo info) {
    // simulation instances don't attach to buses (they instead own one)
    super(null);

    // init bus
    bus = new Bus();

    // init components on bus
    proc = new Processor(bus, info.processorInfo);
    memory = new MemorySpace(bus, info.memoryInfo);

    // loop through device infos and init devices
    for (DeviceInfo deviceInfo : info.devicesInfo) {
      switch (deviceInfo) {
        case VideoInfo videoInfo -> {
          VideoDevice videoDevice = new VideoDevice(bus, memory, videoInfo);
          devices.add(videoDevice);
        }

        case KeyboardInfo keyboardInfo -> {
          KeyboardDevice keyboardDevice = new KeyboardDevice(bus, keyboardInfo);
          devices.add(keyboardDevice);
        }

        case TimerInfo timerInfo -> {
          TimerDevice timerDevice = new TimerDevice(bus, timerInfo);
          devices.add(timerDevice);
        }

        case null, default ->
          throw new RuntimeException("Unkown device in device list");
      }
    }

    // sets self as listener
    //this leaks this but we don't expect listeners to use it before event is raised
    bus.addListener(this);
    proc.addListener(this);
    memory.addListener(this);

    for (IoDevice device : devices) {
      device.addListener(this);
    }
  }

  /**
   * Pipes {@link microsim.simulation.event.FrameEvent} events from
   * {@link microsim.simulation.component.SimulationComponent} components to external
   * {@link microsim.simulation.event.SimulationListener} listeners.
   *
   * @param e event to pipe
   */
  @Override
  public void onSimulationEvent(SimulationEvent e) {
    raiseEvent(e);
  }

  /**
   * Performs a simulation cycle. A cycle is performed by:
   * <ol>
   * <li>Stepping the bus to propagate buffered values.</li>
   * <li>Stepping components. Components are stepped in order:
   * <ol>
   * <li>Processor.</li>
   * <li>Memory space.</li>
   * <li>I/O devices.</li>
   * </ol>
   * </li>
   * </ol>
   */
  @Override
  public final void step() {
    // bus lines take their value
    bus.step();

    // components read and step
    // local bus
    proc.step();
    memory.step();

    // devices
    for (IoDevice device : devices) {
      device.step();
    }
  }

  /**
   * Executes simulation. All components on local bus update as fast as possible. Threaded devices
   * (like video and timer) run on separate threads at fixed frequency.
   */
  public void begin() {
    // start other threads
    for (IoDevice device : devices) {
      if (device instanceof ThreadedIoDevice threadedDevice) {
        threadedDevice.begin();
      }
    }

    // init cycle counter
    long cycle = 0;

    // enter simulation loop
    while (true) {
      // if debugging signal cycle to show debug shell
      if (DebugShell.isDebuggingEnabled()) {
        raiseEvent(new CycleEvent(this, cycle));
      }

      // actually perform simulation step
      step();

      // increase cycle
      cycle++;
    }
  }
}
