package microsim.simulation.component.device.audio.channel;

public abstract class AudioChannel {

  private double freq;

  public void setFreq(double freq) {
    this.freq = freq;
  }

  private double amp = 0.0;

  public void setAmp(double amp) {
    this.amp = amp;
  }

  private double phase = 0.0;

  public abstract double sample(double phaseStep);
}
