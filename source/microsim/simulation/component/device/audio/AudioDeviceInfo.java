package microsim.simulation.component.device.audio;

import microsim.simulation.component.device.audio.channel.AudioChannel;

public class AudioDeviceInfo {

  private final Class<? extends AudioChannel>[] channelTypes;

  public Class<? extends AudioChannel>[] getChannels() {
    return channelTypes;
  }

  public int getChannelNumber() {
    return channelTypes.length;
  }

  @SafeVarargs
  public AudioDeviceInfo(Class<? extends AudioChannel>... channelTypes) {
    this.channelTypes = channelTypes;
  }

  public AudioChannel[] instantiate() {
    AudioChannel[] channels = new AudioChannel[channelTypes.length];

    for (int i = 0; i < channelTypes.length; i++) {
      try {
        channels[i] = channelTypes[i].getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Couldn't instantiate audio device channels.");
      }
    }

    return channels;
  }

  public static AudioDeviceInfo Info_Ricoh2a03 = new AudioDeviceInfo(
    AudioChannel.class
  );
}
