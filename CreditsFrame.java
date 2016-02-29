import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;


public class CreditsFrame extends JFrame {

	public CreditsFrame(String pName)  {
		super("Credits");
        setSize(180, 200);
       	setLocationRelativeTo(null);
        setVisible(true);
		setContentPane(new CreditsPane(pName));
	}
}
