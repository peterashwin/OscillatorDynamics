import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


public class NetworkPanel extends JPanel implements ActionListener {
	NetworkFrame networkFrame;
	PhaseOscillatorSystem phaseOscillatorSystem;

	public NetworkPanel( NetworkFrame parent, PhaseOscillatorSystem pos ) {
		networkFrame = parent;
		phaseOscillatorSystem = pos;

		Timer refreshTimer = new Timer(50, this);
		refreshTimer.start();
	}

	public void actionPerformed(ActionEvent evt) {
		repaint();
	}

	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		if ( (phaseOscillatorSystem != null) && (phaseOscillatorSystem.oscillators != null) ) {
			phaseOscillatorSystem.networkBuilder.render(g, width, height);
		} else {
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.BLACK);
			g.drawString("No System Loaded", (width / 2) - 50, height / 2);

			return;
		}
	}
}
