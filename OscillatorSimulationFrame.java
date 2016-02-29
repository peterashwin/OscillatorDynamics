import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

// changed to sketch network if you want
public class OscillatorSimulationFrame extends JInternalFrame {
	
	public OscillatorSimulationFrame(PhaseOscillatorSystem pos) {
		super("Simulation", //title
				true, //resizeable
				false, //closable
				true, //maximizeable
				false); //iconifiable
		setBorder(BorderFactory.createLineBorder(new Color(0.1f, 0.2f, 0.4f)));
		setSize(400,400);
		setVisible(true);
		setOpaque(true);

		add(new OscillatorSimulationPanel(this, pos));
	}
}

