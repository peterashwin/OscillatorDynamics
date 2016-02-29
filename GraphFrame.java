import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


public class GraphFrame extends JInternalFrame {
	Graphics g;
	
	public GraphFrame(PhaseOscillatorSystem system){
		super("Graph", //title
				true, //resizeable
				false, //closable
				true, //maximizeable
				false); //iconifiable
		setBorder(BorderFactory.createLineBorder(new Color(0.1f, 0.2f, 0.4f)));
		setVisible(true);
		//setOpaque(false);
		GraphPanel p = new GraphPanel(system);
		add(p);
		setGlassPane(new GraphOverlayPanel(system, p));
		getGlassPane().setVisible(true);
	}
	
	
}
