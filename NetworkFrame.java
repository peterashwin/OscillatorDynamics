import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class NetworkFrame extends JInternalFrame {

	public NetworkFrame(PhaseOscillatorSystem pos) {
		super("Network", //title
				true, //resizeable
				false, //closable
				true, //maximizeable
				false); //iconifiable
		setBorder(BorderFactory.createLineBorder(new Color(0.1f, 0.2f, 0.4f)));
		setSize(400,400);
		setVisible(true);
		setOpaque(true);
		
		add(new NetworkPanel(this, pos));
	}
}
