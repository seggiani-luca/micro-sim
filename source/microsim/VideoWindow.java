package microsim;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import microsim.component.*;

class VideoPanel extends JPanel {
	private int scale;
	private final VideoDevice video;

	public VideoPanel(VideoDevice video, int scale) {
		this.video = video;
		this.scale = scale;

		setPreferredSize(new Dimension(
			video.getFrame().getWidth() * scale,
			video.getFrame().getHeight() * scale
		));
	}

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

		// get frame from VideoDevice
		BufferedImage frame = video.getFrame();

		// get scaled dimensions
    int w = frame.getWidth() * scale;
    int h = frame.getHeight() * scale;

		// draw scaled frame
    g2.drawImage(frame, 0, 0, w, h, null);
	}
}

public class VideoWindow {
	private final JFrame frame;
	private final VideoPanel panel;

	public VideoWindow(VideoDevice video, int scale) {
		// setup panel
		panel = new VideoPanel(video, scale);
	
		// setup window
		frame = new JFrame("micro-sim");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}

	public void repaint() {
		panel.repaint();
	}
}
