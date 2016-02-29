import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class OscillatorSimulationPanel extends JPanel implements ActionListener{
	OscillatorSimulationFrame simulatorFrame;
	PhaseOscillatorSystem phaseOscillatorSystem;


	public OscillatorSimulationPanel( OscillatorSimulationFrame parent, PhaseOscillatorSystem system ) {
		simulatorFrame = parent;
		phaseOscillatorSystem = system;

		Timer refreshTimer = new Timer(17, this);
		refreshTimer.start();
	}
	
	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		if ( (phaseOscillatorSystem != null) && (phaseOscillatorSystem.oscillators != null) ) {
			phaseOscillatorSystem.render(g, width, height);
		} else {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.BLACK);
			g.drawString("No System Loaded", (width / 2) - 50, height / 2);

			return;
		}
	}
	
	public void actionPerformed(ActionEvent evt) {
		repaint();
	}
}