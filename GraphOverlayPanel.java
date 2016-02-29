import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;


public class GraphOverlayPanel extends JPanel implements ActionListener {
	PhaseOscillatorSystem phaseOscillatorSystem;
	GraphPanel graphPanel;
	public GraphOverlayPanel(PhaseOscillatorSystem system, GraphPanel graph){
		super(null);
		setOpaque(false);
		graphPanel = graph;
		phaseOscillatorSystem = system;
		Timer refresh = new Timer(25, this);
		refresh.start();
	}
	
	protected void paintComponent(Graphics g){
		render((Graphics2D)g);
	}
	
	protected void render(Graphics2D g) { 
		double[] outValues = phaseOscillatorSystem.integrator.getLastOutValues();
		if(outValues != null){
			double ov;
			int xs, ys;
			float yMax = graphPanel.yMax;
			float xMin = graphPanel.xMin;
			float ctsx = graphPanel.ctsx;
			float ctsy = graphPanel.ctsy;
			CouplingFunction c = (CouplingFunction)phaseOscillatorSystem.oscillators[0].function;
			Color color;
			for(int i = 0; i < outValues.length; i++){
				color = new Color(Color.HSBtoRGB((float)i / outValues.length, 1f, 1f));
				for(int j = 0; j < outValues.length; j++){
					if(i != j){
						//color = new Color(Color.HSBtoRGB((float)j / outValues.length, 1f, 1f));
						ov = outValues[i] - outValues[j];
						ov -= 2 * Math.PI * Math.floor((ov+Math.PI) / (2*Math.PI));
						xs = (int)Math.round((ov - xMin) * ctsx);
						ys = (int)Math.round((yMax - c.gPhi(ov)) * ctsy);
						g.setColor(color);
						g.fillOval(xs - 5, ys - 5, 10, 10);
						g.setColor(Color.BLACK);
						g.drawOval(xs - 5, ys - 5, 10, 10);
					}
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(phaseOscillatorSystem.isRunning())
			repaint();
	}
}
