import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.*;

import java.text.DecimalFormat;
import java.text.ParseException;

public class SystemFrame extends JInternalFrame {

	public SystemFrame(PhaseOscillatorSystem pos) {
		super("Parameters", //title
				true, //resizeable
				false, //closable
				true, //maximizeable
				false); //iconifiable

		setSize(100,300);
		setVisible(true);
		setOpaque(true);
		setBorder(BorderFactory.createLineBorder(new Color(0.1f, 0.2f, 0.4f)));
		add(new SystemPanel(this, pos));
	}
}
