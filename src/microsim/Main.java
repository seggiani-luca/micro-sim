package microsim;

import microsim.component.*;

public class Main {
	public static void main(String[] args) {
		MemorySpace memory = new MemorySpace();
		
		VideoDevice video = new VideoDevice(memory);

		VideoWindow window = new VideoWindow(video);
	}
}
