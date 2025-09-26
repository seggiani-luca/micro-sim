package microsim;

import javax.swing.*;
import java.awt.*;
import microsim.component.*;

class VideoPanel extends JPanel {
	private final VideoDevice video;

	public VideoPanel(VideoDevice video) {
		this.video = video;

		setPreferredSize(new Dimension(
			video.getFrame().getWidth(),
			video.getFrame().getHeight()
		));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(video.getFrame(), 0, 0, null);
	}
}

public class VideoWindow {
	private final JFrame frame;
	private final VideoPanel panel;

	public VideoWindow(VideoDevice video) {
		panel = new VideoPanel(video);
		
		frame = new JFrame("micro-sim");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}

	public void repaint() {
		panel.repaint();
	}
}
