package microsim.simulation.component.device.audio;

import javax.sound.sampled.*;
import microsim.simulation.component.bus.Bus;
import microsim.simulation.component.device.ThreadedIoDevice;
import microsim.simulation.component.device.audio.channel.AudioChannel;

public class AudioDevice extends ThreadedIoDevice {

  public static final int BUFFER_SIZE = 1024;
  public static final float SAMPLE_RATE = 22_050; // half of 44.1 KHz
  public static final int SAMPLE_DEPTH = 8;

  public static final long BUFFER_TIME = (1_000_000_000L / (long) SAMPLE_RATE) * BUFFER_SIZE; // ns

  private SourceDataLine dataLine;

  private AudioChannel[] channels;

  public AudioDevice(Bus bus, int base, AudioDeviceInfo deviceInfo) {
    super(bus, base, 1);

    // initialize channels based on info
    channels = deviceInfo.instantiate();

    // initialize line
    try {
      // initialize audio format: set sample rate, sample depth, 1 channel, signed, little-endian
      AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_DEPTH, 1, true, false);
      DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, format);

      // init line
      dataLine = (SourceDataLine) AudioSystem.getLine(lineInfo);
      dataLine.open(format);

      // start playing
      dataLine.start();
    } catch (LineUnavailableException e) {
      throw new RuntimeException("Couldn't inizialize audio line");
    }
  }

  private double renderSample(double phaseStep) {
    double sample = 0;

    // step through channels querying samples
    for (AudioChannel channel : channels) {
      sample += channel.sample(phaseStep);
    }

    // do basic mixing
    sample /= channels.length;

    return sample;
  }

  @Override
  protected void deviceThread() {
    byte[] buffer = new byte[BUFFER_SIZE];
    final double phaseStep = 1.0 / SAMPLE_RATE;

    while (true) {
      if (dataLine.available() > BUFFER_SIZE) {
        // fill buffer
        for (int i = 0; i < BUFFER_SIZE; i++) {
          buffer[i] = (byte) (renderSample(phaseStep) * 127);
        }
      }
      smartSpin(BUFFER_TIME);
    }
  }

  @Override
  public int getPort(int index) {
    // nothing to return
    return 0;
  }

  @Override
  public void setPort(int index, int data) {

  }

}
