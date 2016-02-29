import javax.swing.*;
import java.awt.*;

public class OscillatorDesktopPane extends JDesktopPane {
	public OscillatorDesktopPane() {
		setOpaque(true);
	}

	public void paint(Graphics g) {
		g.setColor(new Color(0.9f, 0.9f, 0.9f));
		g.fillRect(0, 0, getWidth(), getHeight());

     	super.paintChildren(g);
     	super.paintBorder(g);
	}
}
