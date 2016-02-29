import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;


public class CreditsPane extends JDesktopPane {
	public CreditsPane(String pName) {
		setOpaque(true);
		this.setLayout(new BoxLayout(this,1));
		add(new JLabel("<HTML>"
				+ pName
				+ "<br />This program was written by<br /><br />"
				+ "<p style=\"color:#aa0000;font-size:12px;text-align:left\">Peter Ashwin<br />"
				+ "John Wordsworth<br />"
				+ "David Leppla-Weber<br /><br />University of Exeter <br />"
				+ "2006-2014 </HTML>"));
	}

	public void paint(Graphics g) {
		g.setColor(new Color(0.9f, 0.9f, 0.9f));
		g.fillRect(0, 0, getWidth(), getHeight());

     	super.paintChildren(g);
     	super.paintBorder(g);
	}
}
